package com.shiptrackpro.shipment.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.common.exception.ResourceNotFoundException;
import com.shiptrackpro.common.exception.ShipTrackException;
import com.shiptrackpro.shipment.dto.response.ProofOfDeliveryResponse;
import com.shiptrackpro.shipment.entity.ProofOfDelivery;
import com.shiptrackpro.shipment.entity.Shipment;
import com.shiptrackpro.shipment.enums.ShipmentStatus;
import com.shiptrackpro.shipment.mapper.ShipmentMapper;
import com.shiptrackpro.shipment.repository.ProofOfDeliveryRepository;
import com.shiptrackpro.shipment.repository.ShipmentRepository;
import com.shiptrackpro.shipment.repository.ShipmentStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProofOfDeliveryServiceImplTest {

    @Mock
    private ProofOfDeliveryRepository podRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentStatusHistoryRepository statusHistoryRepository;

    @Mock
    private ShipmentMapper shipmentMapper;

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private ProofOfDeliveryServiceImpl podService;

    private UUID shipmentId;
    private UUID senderId;
    private Shipment shipment;
    private ProofOfDelivery pod;

    @BeforeEach
    void setUp() {
        shipmentId = UUID.randomUUID();
        senderId = UUID.randomUUID();

        shipment = Shipment.builder()
                .trackingNumber("STP-123456")
                .senderId(senderId)
                .status(ShipmentStatus.OUT_FOR_DELIVERY)
                .build();
        shipment.setId(shipmentId);

        pod = ProofOfDelivery.builder()
                .shipment(shipment)
                .receivedBy("Jane Recipient")
                .signatureUrl("http://cloudinary.com/sig.png")
                .photoUrl("http://cloudinary.com/photo.png")
                .build();
    }

    @Test
    void uploadPod_Success() throws IOException {
        MockMultipartFile sig = new MockMultipartFile("signature", "sig.png", "image/png", "sig-bytes".getBytes());
        MockMultipartFile photo = new MockMultipartFile("photo", "photo.jpg", "image/jpeg", "photo-bytes".getBytes());

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(podRepository.existsByShipmentId(shipmentId)).thenReturn(false);

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), any())).thenReturn(Map.of("secure_url", "http://cloudinary.com/img.png"));

        when(podRepository.save(any())).thenReturn(pod);
        when(shipmentMapper.toProofOfDeliveryResponse(any()))
                .thenReturn(ProofOfDeliveryResponse.builder().receivedBy("Jane Recipient").build());

        ProofOfDeliveryResponse resp = podService.uploadPod(shipmentId, "Jane Recipient", "Left at door", sig, photo, "adminId");

        assertNotNull(resp);
        assertEquals("Jane Recipient", resp.getReceivedBy());
        assertEquals(ShipmentStatus.DELIVERED, shipment.getStatus());
        verify(statusHistoryRepository).save(any());
    }

    @Test
    void uploadPod_InvalidStatus_ThrowsException() {
        shipment.setStatus(ShipmentStatus.CREATED);
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));

        assertThrows(ShipTrackException.class, () ->
                podService.uploadPod(shipmentId, "Jane Recipient", "Left at door", null, null, "adminId"));
    }

    @Test
    void getPod_Success_Owner() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(podRepository.findByShipmentId(shipmentId)).thenReturn(Optional.of(pod));
        when(shipmentMapper.toProofOfDeliveryResponse(pod))
                .thenReturn(ProofOfDeliveryResponse.builder().receivedBy("Jane Recipient").build());

        ProofOfDeliveryResponse resp = podService.getPod(shipmentId, senderId, AppConstants.ROLE_CUSTOMER);

        assertNotNull(resp);
        assertEquals("Jane Recipient", resp.getReceivedBy());
    }

    @Test
    void getPod_Forbidden_NonOwner() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));

        assertThrows(ShipTrackException.class, () ->
                podService.getPod(shipmentId, UUID.randomUUID(), AppConstants.ROLE_CUSTOMER));
    }
}
