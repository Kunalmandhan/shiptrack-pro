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
public class StatusDistributionDTO {

    private long total;
    private Map<String, Long> distribution;
    private Map<String, Double> percentageDistribution;
}
