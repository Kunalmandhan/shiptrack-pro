package com.shiptrackpro.tracking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchLocationUpdateRequest {

    @NotEmpty(message = "Location updates list cannot be empty")
    @Valid
    private List<LocationUpdateRequest> updates;
}
