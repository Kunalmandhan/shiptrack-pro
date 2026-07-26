package com.shiptrackpro.shipment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatsResponse {

    private long totalShipments;
    private long createdCount;
    private long pickedUpCount;
    private long inTransitCount;
    private long outForDeliveryCount;
    private long deliveredCount;
    private long failedCount;
    private long cancelledCount;
    private long delayedCount;
    private double onTimeDeliveryRate;
    private double averageDeliveryHours;
    private Map<String, Long> statusDistribution;
}
