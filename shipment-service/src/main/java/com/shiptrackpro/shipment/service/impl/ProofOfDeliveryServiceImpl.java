package com.shiptrackpro.shipment.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.common.exception.DuplicateResourceException;
import com.shiptrackpro.common.exception.ResourceNotFoundException;
import com.shiptrackpro.common.exception.ShipTrackException;
import com.shiptrackpro.shipment.dto.response.ProofOfDeliveryResponse;
import com.shiptrackpro.shipment.entity.ProofOfDelivery;
import com.shiptrackpro.shipment.entity.Shipment;
import com.shiptrackpro.shipment.entity.ShipmentStatusHistory;
import com.shiptrackpro.shipment.enums.ShipmentStatus;
import com.shiptrackpro.shipment.mapper.ShipmentMapper;
import com.shiptrackpro.shipment.repository.ProofOfDeliveryRepository;
import com.shiptrackpro.shipment.repository.ShipmentRepository;
import com.shiptrackpro.shipment.repository.ShipmentStatusHistoryRepository;
import com.shiptrackpro.shipment.service.ProofOfDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProofOfDeliveryServiceImpl implements ProofOfDeliveryService {

    private final ProofOfDeliveryRepository podRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentStatusHistoryRepository statusHistoryRepository;
    private final ShipmentMapper shipmentMapper;
    private final Cloudinary cloudinary;

    @Override
    public ProofOfDeliveryResponse uploadPod(UUID shipmentId, String receivedBy, String notes,
                                              MultipartFile signature, MultipartFile photo, String userId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId.toString()));

        // Validate shipment is in correct status
        if (shipment.getStatus() != ShipmentStatus.OUT_FOR_DELIVERY) {
            throw new ShipTrackException(
                    "POD can only be uploaded when shipment is OUT_FOR_DELIVERY. Current: " + shipment.getStatus(),
                    "INVALID_POD_STATUS",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Check for existing POD
        if (podRepository.existsByShipmentId(shipmentId)) {
            throw new DuplicateResourceException("ProofOfDelivery", "shipmentId", shipmentId.toString());
        }

        // Upload files to Cloudinary
        String signatureUrl = uploadToCloudinary(signature, "pod/signatures/" + shipmentId);
        String photoUrl = uploadToCloudinary(photo, "pod/photos/" + shipmentId);

        // Create POD
        ProofOfDelivery pod = ProofOfDelivery.builder()
                .shipment(shipment)
                .signatureUrl(signatureUrl)
                .photoUrl(photoUrl)
                .receivedBy(receivedBy)
                .notes(notes)
                .deliveredAt(LocalDateTime.now())
                .build();

        pod = podRepository.save(pod);

        // Transition shipment to DELIVERED
        ShipmentStatus previousStatus = shipment.getStatus();
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setActualDelivery(LocalDateTime.now());
        shipmentRepository.save(shipment);

        // Record status change
        ShipmentStatusHistory history = ShipmentStatusHistory.builder()
                .shipment(shipment)
                .fromStatus(previousStatus)
                .toStatus(ShipmentStatus.DELIVERED)
                .changedBy(userId)
                .notes("POD uploaded — received by: " + receivedBy)
                .build();
        statusHistoryRepository.save(history);

        // Release driver and vehicle
        if (shipment.getAssignedDriver() != null) {
            shipment.getAssignedDriver().setAvailable(true);
        }
        if (shipment.getAssignedVehicle() != null) {
            shipment.getAssignedVehicle().setAvailable(true);
        }

        log.info("POD uploaded for shipment {}, delivered to: {}", shipment.getTrackingNumber(), receivedBy);
        return shipmentMapper.toProofOfDeliveryResponse(pod);
    }

    @Override
    @Transactional(readOnly = true)
    public ProofOfDeliveryResponse getPod(UUID shipmentId, UUID userId, String role) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId.toString()));

        // Authorization
        if (!AppConstants.ROLE_ADMIN.equalsIgnoreCase(role)
                && !shipment.getSenderId().equals(userId)) {
            throw new ShipTrackException(
                    "You do not have permission to view this POD",
                    "ACCESS_DENIED",
                    HttpStatus.FORBIDDEN
            );
        }

        ProofOfDelivery pod = podRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("ProofOfDelivery", "shipmentId", shipmentId.toString()));

        return shipmentMapper.toProofOfDeliveryResponse(pod);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadPodPhoto(UUID shipmentId, UUID userId, String role) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId.toString()));

        // Authorization
        if (!AppConstants.ROLE_ADMIN.equalsIgnoreCase(role)
                && !shipment.getSenderId().equals(userId)) {
            throw new ShipTrackException(
                    "You do not have permission to download this POD",
                    "ACCESS_DENIED",
                    HttpStatus.FORBIDDEN
            );
        }

        ProofOfDelivery pod = podRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("ProofOfDelivery", "shipmentId", shipmentId.toString()));

        if (pod.getPhotoUrl() == null || pod.getPhotoUrl().isBlank()) {
            throw new ShipTrackException(
                    "No photo available for this POD",
                    "POD_NO_PHOTO",
                    HttpStatus.NOT_FOUND
            );
        }

        try {
            URL url = new URL(pod.getPhotoUrl());
            try (InputStream in = url.openStream()) {
                return in.readAllBytes();
            }
        } catch (IOException e) {
            log.error("Failed to download POD photo for shipment {}: {}", shipmentId, e.getMessage());
            throw new ShipTrackException(
                    "Failed to download POD photo",
                    "POD_DOWNLOAD_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // ==================== Private Helpers ====================

    @SuppressWarnings("unchecked")
    private String uploadToCloudinary(MultipartFile file, String publicId) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", "shiptrack-pro",
                            "resource_type", "auto"
                    )
            );
            return (String) result.get("secure_url");
        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary: {}", e.getMessage());
            throw new ShipTrackException(
                    "Failed to upload file",
                    "FILE_UPLOAD_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
