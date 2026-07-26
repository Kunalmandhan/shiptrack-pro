package com.shiptrackpro.tracking.dto.response;

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
public class LocationResponse {

    private UUID shipmentId;
    private UUID driverId;
    private Double latitude;
    private Double longitude;
    private Double speedKmh;
    private Double headingDegrees;
    private Double altitudeM;
    private LocalDateTime recordedAt;
}
