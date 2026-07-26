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
public class DelayAnalysisDTO {

    private long totalDelayed;
    private double delayPercentage;
    private double avgDelayHours;
    private Map<String, Long> delayReasons;
}
