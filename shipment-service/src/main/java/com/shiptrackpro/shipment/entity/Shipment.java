package com.shiptrackpro.shipment.entity;

import com.shiptrackpro.common.entity.BaseEntity;
import com.shiptrackpro.shipment.enums.PackageType;
import com.shiptrackpro.shipment.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Core shipment entity — represents a package being shipped from sender to receiver.
 *
 * Lifecycle:
 * CREATED → PROCESSING → PICKED_UP → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
 *
 * The senderId links to the User Service (user who created the shipment).
 * Driver and vehicle are assigned by admins during the PROCESSING stage.
 */
@Entity
@Table(name = "shipments", schema = "shiptrack_shipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment extends BaseEntity {

    @Column(name = "tracking_number", nullable = false, unique = true, length = 12)
    private String trackingNumber;

    // --- Sender (linked to User Service) ---
    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "sender_name", nullable = false, length = 100)
    private String senderName;

    @Column(name = "sender_email", nullable = false, length = 255)
    private String senderEmail;

    @Column(name = "sender_phone", nullable = false, length = 20)
    private String senderPhone;

    @Column(name = "origin_address", nullable = false, length = 500)
    private String originAddress;

    @Column(name = "origin_lat")
    private Double originLat;

    @Column(name = "origin_lng")
    private Double originLng;

    // --- Receiver ---
    @Column(name = "receiver_id")
    private UUID receiverId;

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_email", nullable = false, length = 255)
    private String receiverEmail;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(name = "destination_address", nullable = false, length = 500)
    private String destinationAddress;

    @Column(name = "destination_lat")
    private Double destinationLat;

    @Column(name = "destination_lng")
    private Double destinationLng;

    // --- Shipment Details ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.CREATED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_driver_id")
    private Driver assignedDriver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_vehicle_id")
    private Vehicle assignedVehicle;

    @Column(name = "weight_kg", nullable = false)
    private Double weightKg;

    @Column(length = 50)
    private String dimensions;

    @Enumerated(EnumType.STRING)
    @Column(name = "package_type", nullable = false, length = 20)
    private PackageType packageType;

    @Column(length = 1000)
    private String description;

    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;

    @Column(name = "actual_delivery")
    private LocalDateTime actualDelivery;

    // --- Status History ---
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<ShipmentStatusHistory> statusHistory = new ArrayList<>();

    // --- Proof of Delivery ---
    @OneToOne(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProofOfDelivery proofOfDelivery;
}
