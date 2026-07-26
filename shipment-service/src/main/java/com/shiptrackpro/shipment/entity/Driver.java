package com.shiptrackpro.shipment.entity;

import com.shiptrackpro.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Driver entity — represents a delivery driver who can be assigned to shipments.
 *
 * Drivers are managed by admins. The 'available' flag controls whether
 * they can be assigned to new shipments. Current location is updated
 * by the Tracking Service via WebSocket in a later step.
 */
@Entity
@Table(name = "drivers", schema = "shiptrack_shipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Column(nullable = false)
    @Builder.Default
    private boolean available = true;

    @Column(name = "current_lat")
    private Double currentLat;

    @Column(name = "current_lng")
    private Double currentLng;
}
