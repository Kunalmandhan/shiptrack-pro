package com.shiptrackpro.shipment.dto.response;

import com.shiptrackpro.shipment.enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Public tracking response — no sender details exposed.
 * Returned for the public /api/v1/shipments/track/{trackingNumber} endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentTrackingResponse {

    private String trackingNumber;
    private ShipmentStatus status;

    // Only destination is shown publicly
    private String destinationAddress;

    // Package summary
    private String packageType;
    private Double weightKg;

    // Timestamps
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private LocalDateTime createdAt;

    // Status timeline (public — no changedBy user details)
    private List<StatusHistoryResponse> statusHistory;
}
