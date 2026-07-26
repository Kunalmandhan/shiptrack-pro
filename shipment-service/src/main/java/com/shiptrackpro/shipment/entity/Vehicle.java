package com.shiptrackpro.shipment.entity;

import com.shiptrackpro.common.entity.BaseEntity;
import com.shiptrackpro.shipment.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Vehicle entity — represents a delivery vehicle that can be assigned to shipments.
 *
 * Vehicles are managed by admins. The 'available' flag controls whether
 * they can be assigned to new shipments.
 */
@Entity
@Table(name = "vehicles", schema = "shiptrack_shipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends BaseEntity {

    @Column(name = "plate_number", nullable = false, unique = true, length = 20)
    private String plateNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleType type;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "capacity_kg", nullable = false)
    private Double capacityKg;

    @Column(nullable = false)
    @Builder.Default
    private boolean available = true;
}
