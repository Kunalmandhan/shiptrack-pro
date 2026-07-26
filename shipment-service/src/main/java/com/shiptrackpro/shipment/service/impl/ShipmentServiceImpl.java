package com.shiptrackpro.shipment.service.impl;

import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.common.exception.InvalidStateTransitionException;
import com.shiptrackpro.common.exception.ResourceNotFoundException;
import com.shiptrackpro.common.exception.ShipTrackException;
import com.shiptrackpro.shipment.dto.request.AssignShipmentRequest;
import com.shiptrackpro.shipment.dto.request.CreateShipmentRequest;
import com.shiptrackpro.shipment.dto.request.UpdateStatusRequest;
import com.shiptrackpro.shipment.dto.response.ShipmentDetailResponse;
import com.shiptrackpro.shipment.dto.response.ShipmentResponse;
import com.shiptrackpro.shipment.dto.response.ShipmentTrackingResponse;
import com.shiptrackpro.shipment.entity.Driver;
import com.shiptrackpro.shipment.entity.Shipment;
import com.shiptrackpro.shipment.entity.ShipmentStatusHistory;
import com.shiptrackpro.shipment.entity.Vehicle;
import com.shiptrackpro.shipment.enums.ShipmentStatus;
import com.shiptrackpro.shipment.mapper.ShipmentMapper;
import com.shiptrackpro.shipment.repository.DriverRepository;
import com.shiptrackpro.shipment.repository.ShipmentRepository;
import com.shiptrackpro.shipment.repository.ShipmentStatusHistoryRepository;
import com.shiptrackpro.shipment.repository.VehicleRepository;
import com.shiptrackpro.shipment.service.ShipmentService;
import com.shiptrackpro.shipment.util.TrackingNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentStatusHistoryRepository statusHistoryRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final ShipmentMapper shipmentMapper;
    private final TrackingNumberGenerator trackingNumberGenerator;

    @Override
    public ShipmentDetailResponse createShipment(UUID senderId, CreateShipmentRequest request) {
        String trackingNumber = trackingNumberGenerator.generate();

        Shipment shipment = Shipment.builder()
                .trackingNumber(trackingNumber)
                .senderId(senderId)
                .senderName(request.getSenderName())
                .senderEmail(request.getSenderEmail())
                .senderPhone(request.getSenderPhone())
                .originAddress(request.getOriginAddress())
                .originLat(request.getOriginLat())
                .originLng(request.getOriginLng())
                .receiverName(request.getReceiverName())
                .receiverEmail(request.getReceiverEmail())
                .receiverPhone(request.getReceiverPhone())
                .destinationAddress(request.getDestinationAddress())
                .destinationLat(request.getDestinationLat())
                .destinationLng(request.getDestinationLng())
                .weightKg(request.getWeightKg())
                .dimensions(request.getDimensions())
                .packageType(request.getPackageType())
                .description(request.getDescription())
                .status(ShipmentStatus.CREATED)
                .build();

        shipment = shipmentRepository.save(shipment);

        // Record initial status in history
        recordStatusChange(shipment, null, ShipmentStatus.CREATED, senderId.toString(), "Shipment created");

        log.info("Shipment created: {} by sender: {}", trackingNumber, senderId);
        return shipmentMapper.toShipmentDetailResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentDetailResponse getShipmentById(UUID id, UUID userId, String role) {
        Shipment shipment = findShipmentOrThrow(id);

        // Authorization: admin can see any shipment, customer can only see their own
        if (!AppConstants.ROLE_ADMIN.equalsIgnoreCase(role)
                && !shipment.getSenderId().equals(userId)) {
            throw new ShipTrackException(
                    "You do not have permission to view this shipment",
                    "ACCESS_DENIED",
                    HttpStatus.FORBIDDEN
            );
        }

        return shipmentMapper.toShipmentDetailResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getAllShipments(Pageable pageable) {
        return shipmentRepository.findAll(pageable)
                .map(shipmentMapper::toShipmentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getMyShipments(UUID senderId, Pageable pageable) {
        return shipmentRepository.findBySenderId(senderId, pageable)
                .map(shipmentMapper::toShipmentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentTrackingResponse trackShipment(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "trackingNumber", trackingNumber));

        return shipmentMapper.toShipmentTrackingResponse(shipment);
    }

    @Override
    public ShipmentDetailResponse updateStatus(UUID id, UpdateStatusRequest request, String userId) {
        Shipment shipment = findShipmentOrThrow(id);
        ShipmentStatus currentStatus = shipment.getStatus();
        ShipmentStatus newStatus = request.getStatus();

        // Validate state transition
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStateTransitionException(currentStatus.name(), newStatus.name());
        }

        // Record status change
        recordStatusChange(shipment, currentStatus, newStatus, userId, request.getNotes());

        // Update shipment status
        shipment.setStatus(newStatus);

        // If delivered, set actual delivery time
        if (newStatus == ShipmentStatus.DELIVERED) {
            shipment.setActualDelivery(LocalDateTime.now());
        }

        shipment = shipmentRepository.save(shipment);
        log.info("Shipment {} status updated: {} → {} by user: {}",
                shipment.getTrackingNumber(), currentStatus, newStatus, userId);

        return shipmentMapper.toShipmentDetailResponse(shipment);
    }

    @Override
    public ShipmentDetailResponse assignShipment(UUID id, AssignShipmentRequest request, String userId) {
        Shipment shipment = findShipmentOrThrow(id);

        // Must be in CREATED status to assign
        if (shipment.getStatus() != ShipmentStatus.CREATED) {
            throw new ShipTrackException(
                    "Shipment can only be assigned when in CREATED status. Current: " + shipment.getStatus(),
                    "INVALID_ASSIGNMENT",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Validate and fetch driver
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", request.getDriverId().toString()));
        if (!driver.isAvailable()) {
            throw new ShipTrackException(
                    "Driver '" + driver.getName() + "' is not available",
                    "DRIVER_UNAVAILABLE",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Validate and fetch vehicle
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", request.getVehicleId().toString()));
        if (!vehicle.isAvailable()) {
            throw new ShipTrackException(
                    "Vehicle '" + vehicle.getPlateNumber() + "' is not available",
                    "VEHICLE_UNAVAILABLE",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Assign driver and vehicle
        shipment.setAssignedDriver(driver);
        shipment.setAssignedVehicle(vehicle);

        // Auto-transition to PROCESSING
        ShipmentStatus currentStatus = shipment.getStatus();
        shipment.setStatus(ShipmentStatus.PROCESSING);
        recordStatusChange(shipment, currentStatus, ShipmentStatus.PROCESSING, userId,
                "Assigned driver: " + driver.getName() + ", vehicle: " + vehicle.getPlateNumber());

        // Mark driver and vehicle as unavailable
        driver.setAvailable(false);
        vehicle.setAvailable(false);
        driverRepository.save(driver);
        vehicleRepository.save(vehicle);

        shipment = shipmentRepository.save(shipment);
        log.info("Shipment {} assigned to driver: {}, vehicle: {} by admin: {}",
                shipment.getTrackingNumber(), driver.getName(), vehicle.getPlateNumber(), userId);

        return shipmentMapper.toShipmentDetailResponse(shipment);
    }

    @Override
    public ShipmentDetailResponse cancelShipment(UUID id, UUID userId, String role) {
        Shipment shipment = findShipmentOrThrow(id);

        // Authorization: admin can cancel any, customer can only cancel their own
        if (!AppConstants.ROLE_ADMIN.equalsIgnoreCase(role)
                && !shipment.getSenderId().equals(userId)) {
            throw new ShipTrackException(
                    "You do not have permission to cancel this shipment",
                    "ACCESS_DENIED",
                    HttpStatus.FORBIDDEN
            );
        }

        ShipmentStatus currentStatus = shipment.getStatus();

        // Validate state transition
        if (!currentStatus.canTransitionTo(ShipmentStatus.CANCELLED)) {
            throw new InvalidStateTransitionException(currentStatus.name(), ShipmentStatus.CANCELLED.name());
        }

        // Record status change
        recordStatusChange(shipment, currentStatus, ShipmentStatus.CANCELLED, userId.toString(), "Shipment cancelled");

        // If driver/vehicle were assigned, mark them as available again
        if (shipment.getAssignedDriver() != null) {
            shipment.getAssignedDriver().setAvailable(true);
            driverRepository.save(shipment.getAssignedDriver());
        }
        if (shipment.getAssignedVehicle() != null) {
            shipment.getAssignedVehicle().setAvailable(true);
            vehicleRepository.save(shipment.getAssignedVehicle());
        }

        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipment = shipmentRepository.save(shipment);

        log.info("Shipment {} cancelled by user: {}", shipment.getTrackingNumber(), userId);
        return shipmentMapper.toShipmentDetailResponse(shipment);
    }

    // ==================== Private Helpers ====================

    private Shipment findShipmentOrThrow(UUID id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", id.toString()));
    }

    private void recordStatusChange(Shipment shipment, ShipmentStatus from, ShipmentStatus to,
                                     String changedBy, String notes) {
        ShipmentStatusHistory history = ShipmentStatusHistory.builder()
                .shipment(shipment)
                .fromStatus(from)
                .toStatus(to)
                .changedBy(changedBy)
                .notes(notes)
                .build();
        statusHistoryRepository.save(history);
    }
}
