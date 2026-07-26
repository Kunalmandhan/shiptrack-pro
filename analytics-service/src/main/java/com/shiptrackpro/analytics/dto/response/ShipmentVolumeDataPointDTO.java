package com.shiptrackpro.analytics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentVolumeDataPointDTO {

    private String label;
    private long total;
    private long delivered;
    private long delayed;
}
