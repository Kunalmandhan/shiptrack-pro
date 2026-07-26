package com.shiptrackpro.analytics.repository;

import com.shiptrackpro.analytics.entity.ReportExport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportExportRepository extends JpaRepository<ReportExport, UUID> {

    List<ReportExport> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
