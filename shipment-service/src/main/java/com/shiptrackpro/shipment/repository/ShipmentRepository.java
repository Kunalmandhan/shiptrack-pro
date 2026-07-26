package com.shiptrackpro.shipment.repository;

import com.shiptrackpro.shipment.entity.Shipment;
import com.shiptrackpro.shipment.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    boolean existsByTrackingNumber(String trackingNumber);

    Page<Shipment> findBySenderId(UUID senderId, Pageable pageable);

    Page<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);

    long countByStatus(ShipmentStatus status);

    long countBySenderId(UUID senderId);

    long countBySenderIdAndStatus(UUID senderId, ShipmentStatus status);
}
