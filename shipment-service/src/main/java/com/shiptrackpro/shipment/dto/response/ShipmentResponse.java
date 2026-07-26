package com.shiptrackpro.shipment.dto.response;

import com.shiptrackpro.shipment.enums.PackageType;
import com.shiptrackpro.shipment.enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Standard shipment response for list views.
 * Does not include full status history (use ShipmentDetailResponse for that).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {

    private UUID id;
    private String trackingNumber;
    private ShipmentStatus status;

    // Sender summary
    private String senderName;
    private String originAddress;

    // Receiver summary
    private String receiverName;
    private String destinationAddress;

    // Package summary
    private Double weightKg;
    private PackageType packageType;

    // Assignment
    private String assignedDriverName;
    private String assignedVehiclePlate;

    // Timestamps
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
