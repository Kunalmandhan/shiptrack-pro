package com.shiptrackpro.analytics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {

    private long totalShipments;
    private long activeShipments;
    private long deliveredShipments;
    private long delayedShipments;
    private long cancelledShipments;
    private double onTimeDeliveryRate;
    private double avgDeliveryHours;

    private Map<String, Long> statusDistribution;
}
