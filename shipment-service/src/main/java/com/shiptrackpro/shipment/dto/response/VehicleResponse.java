package com.shiptrackpro.shipment.dto.response;

import com.shiptrackpro.shipment.enums.VehicleType;
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
public class VehicleResponse {

    private UUID id;
    private String plateNumber;
    private VehicleType type;
    private String model;
    private Double capacityKg;
    private boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
