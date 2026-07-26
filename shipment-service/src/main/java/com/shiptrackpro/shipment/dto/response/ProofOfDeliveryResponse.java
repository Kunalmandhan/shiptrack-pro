package com.shiptrackpro.shipment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProofOfDeliveryResponse {

    private UUID id;
    private UUID shipmentId;
    private String signatureUrl;
    private String photoUrl;
    private String receivedBy;
    private String notes;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
}
