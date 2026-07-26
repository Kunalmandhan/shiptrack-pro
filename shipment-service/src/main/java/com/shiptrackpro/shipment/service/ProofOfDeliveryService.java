package com.shiptrackpro.shipment.service;

import com.shiptrackpro.shipment.dto.response.ProofOfDeliveryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ProofOfDeliveryService {

    ProofOfDeliveryResponse uploadPod(UUID shipmentId, String receivedBy, String notes,
                                       MultipartFile signature, MultipartFile photo, String userId);

    ProofOfDeliveryResponse getPod(UUID shipmentId, UUID userId, String role);

    byte[] downloadPodPhoto(UUID shipmentId, UUID userId, String role);
}
