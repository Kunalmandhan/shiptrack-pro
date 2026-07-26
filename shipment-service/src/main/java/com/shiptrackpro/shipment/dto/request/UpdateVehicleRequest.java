package com.shiptrackpro.shipment.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVehicleRequest {

    @Size(min = 2, max = 100, message = "Model must be between 2 and 100 characters")
    private String model;

    @Positive(message = "Capacity must be positive")
    private Double capacityKg;

    private Boolean available;
}
