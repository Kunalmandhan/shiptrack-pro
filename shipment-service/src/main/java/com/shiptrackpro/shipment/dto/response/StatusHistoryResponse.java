package com.shiptrackpro.shipment.dto.response;

import com.shiptrackpro.shipment.enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Single entry in the shipment status timeline.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistoryResponse {

    private UUID id;
    private ShipmentStatus fromStatus;
    private ShipmentStatus toStatus;
    private String changedBy;
    private String notes;
    private LocalDateTime createdAt;
}
