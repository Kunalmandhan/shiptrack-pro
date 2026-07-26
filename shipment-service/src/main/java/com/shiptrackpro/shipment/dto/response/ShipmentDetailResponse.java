package com.shiptrackpro.shipment.dto.response;

import com.shiptrackpro.shipment.enums.PackageType;
import com.shiptrackpro.shipment.enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Full shipment detail response — includes complete status history.
 * Returned when getting a single shipment by ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentDetailResponse {

    private UUID id;
    private String trackingNumber;
    private ShipmentStatus status;

    // Sender
    private UUID senderId;
    private String senderName;
    private String senderEmail;
    private String senderPhone;
    private String originAddress;
    private Double originLat;
    private Double originLng;

    // Receiver
    private UUID receiverId;
    private String receiverName;
    private String receiverEmail;
    private String receiverPhone;
    private String destinationAddress;
    private Double destinationLat;
    private Double destinationLng;

    // Package details
    private Double weightKg;
    private String dimensions;
    private PackageType packageType;
    private String description;

    // Assignment
    private DriverResponse assignedDriver;
    private VehicleResponse assignedVehicle;

    // Timestamps
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Status timeline
    private List<StatusHistoryResponse> statusHistory;

    // Proof of delivery
    private ProofOfDeliveryResponse proofOfDelivery;
}
