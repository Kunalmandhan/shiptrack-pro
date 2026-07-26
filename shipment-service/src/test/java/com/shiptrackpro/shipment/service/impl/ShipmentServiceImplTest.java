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
import com.shiptrackpro.shipment.entity.Vehicle;
import com.shiptrackpro.shipment.enums.PackageType;
import com.shiptrackpro.shipment.enums.ShipmentStatus;
import com.shiptrackpro.shipment.enums.VehicleType;
import com.shiptrackpro.shipment.mapper.ShipmentMapper;
import com.shiptrackpro.shipment.repository.DriverRepository;
import com.shiptrackpro.shipment.repository.ShipmentRepository;
import com.shiptrackpro.shipment.repository.ShipmentStatusHistoryRepository;
import com.shiptrackpro.shipment.repository.VehicleRepository;
import com.shiptrackpro.shipment.util.TrackingNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceImplTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentStatusHistoryRepository statusHistoryRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ShipmentMapper shipmentMapper;

    @Mock
    private TrackingNumberGenerator trackingNumberGenerator;

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    private UUID senderId;
    private UUID shipmentId;
    private Shipment sampleShipment;
    private CreateShipmentRequest createRequest;

    @BeforeEach
    void setUp() {
        senderId = UUID.randomUUID();
        shipmentId = UUID.randomUUID();

        sampleShipment = Shipment.builder()
                .trackingNumber("STP-123456")
                .senderId(senderId)
                .senderName("John Doe")
                .senderEmail("john@example.com")
                .senderPhone("1234567890")
                .originAddress("123 Origin St")
                .receiverName("Jane Smith")
                .receiverEmail("jane@example.com")
                .receiverPhone("0987654321")
                .destinationAddress("456 Dest St")
                .weightKg(5.0)
                .packageType(PackageType.PARCEL)
                .status(ShipmentStatus.CREATED)
                .build();
        sampleShipment.setId(shipmentId);

        createRequest = CreateShipmentRequest.builder()
                .senderName("John Doe")
                .senderEmail("john@example.com")
                .senderPhone("1234567890")
                .originAddress("123 Origin St")
                .receiverName("Jane Smith")
                .receiverEmail("jane@example.com")
                .receiverPhone("0987654321")
                .destinationAddress("456 Dest St")
                .weightKg(5.0)
                .packageType(PackageType.PARCEL)
                .build();
    }

    @Test
    void createShipment_Success() {
        when(trackingNumberGenerator.generate()).thenReturn("STP-123456");
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(sampleShipment);
        when(shipmentMapper.toShipmentDetailResponse(any(Shipment.class)))
                .thenReturn(ShipmentDetailResponse.builder().trackingNumber("STP-123456").status(ShipmentStatus.CREATED).build());

        ShipmentDetailResponse response = shipmentService.createShipment(senderId, createRequest);

        assertNotNull(response);
        assertEquals("STP-123456", response.getTrackingNumber());
        verify(shipmentRepository).save(any(Shipment.class));
        verify(statusHistoryRepository).save(any());
    }

    @Test
    void getShipmentById_Success_Admin() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(sampleShipment));
        when(shipmentMapper.toShipmentDetailResponse(sampleShipment))
                .thenReturn(ShipmentDetailResponse.builder().id(shipmentId).build());

        ShipmentDetailResponse response = shipmentService.getShipmentById(shipmentId, UUID.randomUUID(), AppConstants.ROLE_ADMIN);

        assertNotNull(response);
        assertEquals(shipmentId, response.getId());
    }

    @Test
    void getShipmentById_Success_Owner() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(sampleShipment));
        when(shipmentMapper.toShipmentDetailResponse(sampleShipment))
                .thenReturn(ShipmentDetailResponse.builder().id(shipmentId).build());

        ShipmentDetailResponse response = shipmentService.getShipmentById(shipmentId, senderId, AppConstants.ROLE_CUSTOMER);

        assertNotNull(response);
        assertEquals(shipmentId, response.getId());
    }

    @Test
    void getShipmentById_Forbidden_NonOwner() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(sampleShipment));

        assertThrows(ShipTrackException.class, () ->
                shipmentService.getShipmentById(shipmentId, UUID.randomUUID(), AppConstants.ROLE_CUSTOMER));
    }

    @Test
    void getShipmentById_NotFound() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                shipmentService.getShipmentById(shipmentId, senderId, AppConstants.ROLE_ADMIN));
    }

    @Test
    void getAllShipments_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Shipment> page = new PageImpl<>(List.of(sampleShipment));
        when(shipmentRepository.findAll(pageable)).thenReturn(page);
        when(shipmentMapper.toShipmentResponse(any(Shipment.class))).thenReturn(ShipmentResponse.builder().build());

        Page<ShipmentResponse> result = shipmentService.getAllShipments(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getMyShipments_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Shipment> page = new PageImpl<>(List.of(sampleShipment));
        when(shipmentRepository.findBySenderId(senderId, pageable)).thenReturn(page);
        when(shipmentMapper.toShipmentResponse(any(Shipment.class))).thenReturn(ShipmentResponse.builder().build());

        Page<ShipmentResponse> result = shipmentService.getMyShipments(senderId, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void trackShipment_Success() {
        when(shipmentRepository.findByTrackingNumber("STP-123456")).thenReturn(Optional.of(sampleShipment));
        when(shipmentMapper.toShipmentTrackingResponse(sampleShipment))
                .thenReturn(ShipmentTrackingResponse.builder().trackingNumber("STP-123456").build());

        ShipmentTrackingResponse response = shipmentService.trackShipment("STP-123456");

        assertNotNull(response);
        assertEquals("STP-123456", response.getTrackingNumber());
    }

    @Test
    void updateStatus_Success_ValidTransition() {
        sampleShipment.setStatus(ShipmentStatus.CREATED);
        UpdateStatusRequest req = UpdateStatusRequest.builder().status(ShipmentStatus.PROCESSING).notes("Processing").build();

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(sampleShipment));
        when(shipmentRepository.save(any())).thenReturn(sampleShipment);
        when(shipmentMapper.toShipmentDetailResponse(any()))
                .thenReturn(ShipmentDetailResponse.builder().status(ShipmentStatus.PROCESSING).build());

        ShipmentDetailResponse response = shipmentService.updateStatus(shipmentId, req, "adminId");

        assertNotNull(response);
        assertEquals(ShipmentStatus.PROCESSING, response.getStatus());
        verify(statusHistoryRepository).save(any());
    }

    @Test
    void updateStatus_InvalidTransition_ThrowsException() {
        sampleShipment.setStatus(ShipmentStatus.CREATED);
        UpdateStatusRequest req = UpdateStatusRequest.builder().status(ShipmentStatus.DELIVERED).build();

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(sampleShipment));

        assertThrows(InvalidStateTransitionException.class, () ->
                shipmentService.updateStatus(shipmentId, req, "adminId"));
    }

    @Test
    void assignShipment_Success() {
        UUID driverId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        Driver driver = Driver.builder().name("Driver Bob").available(true).build();
        driver.setId(driverId);
        Vehicle vehicle = Vehicle.builder().plateNumber("XYZ-123").type(VehicleType.VAN).available(true).build();
        vehicle.setId(vehicleId);

        AssignShipmentRequest req = AssignShipmentRequest.builder().driverId(driverId).vehicleId(vehicleId).build();

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(sampleShipment));
        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(shipmentRepository.save(any())).thenReturn(sampleShipment);
        when(shipmentMapper.toShipmentDetailResponse(any()))
                .thenReturn(ShipmentDetailResponse.builder().status(ShipmentStatus.PROCESSING).build());

        ShipmentDetailResponse response = shipmentService.assignShipment(shipmentId, req, "adminId");

        assertNotNull(response);
        assertFalse(driver.isAvailable());
        assertFalse(vehicle.isAvailable());
        verify(driverRepository).save(driver);
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void cancelShipment_Success() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(sampleShipment));
        when(shipmentRepository.save(any())).thenReturn(sampleShipment);
        when(shipmentMapper.toShipmentDetailResponse(any()))
                .thenReturn(ShipmentDetailResponse.builder().status(ShipmentStatus.CANCELLED).build());

        ShipmentDetailResponse response = shipmentService.cancelShipment(shipmentId, senderId, AppConstants.ROLE_CUSTOMER);

        assertNotNull(response);
        assertEquals(ShipmentStatus.CANCELLED, response.getStatus());
    }
}
