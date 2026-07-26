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
public class CustomerDashboardDTO {

    private long totalShipments;
    private long activeShipments;
    private long deliveredShipments;
    private long delayedShipments;
    private double onTimeDeliveryRate;

    private Map<String, Long> statusDistribution;
}
