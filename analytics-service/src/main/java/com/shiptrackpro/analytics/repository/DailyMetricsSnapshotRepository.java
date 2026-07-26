package com.shiptrackpro.analytics.repository;

import com.shiptrackpro.analytics.entity.DailyMetricsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyMetricsSnapshotRepository extends JpaRepository<DailyMetricsSnapshot, UUID> {

    Optional<DailyMetricsSnapshot> findBySnapshotDate(LocalDate snapshotDate);

    List<DailyMetricsSnapshot> findBySnapshotDateBetweenOrderBySnapshotDateAsc(LocalDate startDate, LocalDate endDate);
}
