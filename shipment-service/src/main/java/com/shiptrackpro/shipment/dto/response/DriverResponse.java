package com.shiptrackpro.shipment.dto.response;

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
public class DriverResponse {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String licenseNumber;
    private boolean available;
    private Double currentLat;
    private Double currentLng;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
