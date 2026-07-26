package com.shiptrackpro.tracking.repository;

import com.shiptrackpro.tracking.entity.TrackingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrackingHistoryRepository extends JpaRepository<TrackingHistory, UUID> {

    List<TrackingHistory> findByShipmentIdOrderByRecordedAtAsc(UUID shipmentId);

    Page<TrackingHistory> findByShipmentIdOrderByRecordedAtDesc(UUID shipmentId, Pageable pageable);

    Optional<TrackingHistory> findFirstByShipmentIdOrderByRecordedAtDesc(UUID shipmentId);

    Optional<TrackingHistory> findFirstByDriverIdOrderByRecordedAtDesc(UUID driverId);
}
