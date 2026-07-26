package com.shiptrackpro.shipment.dto.request;

import com.shiptrackpro.shipment.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private ShipmentStatus status;

    @Size(max = 500, message = "Notes must be at most 500 characters")
    private String notes;
}
