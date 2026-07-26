package com.shiptrackpro.analytics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "daily_metrics_snapshots", schema = "shiptrack_analytics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMetricsSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "snapshot_date", nullable = false, unique = true)
    private LocalDate snapshotDate;

    @Column(name = "total_shipments", nullable = false)
    private Long totalShipments;

    @Column(name = "delivered_count", nullable = false)
    private Long deliveredCount;

    @Column(name = "delayed_count", nullable = false)
    private Long delayedCount;

    @Column(name = "cancelled_count", nullable = false)
    private Long cancelledCount;

    @Column(name = "on_time_delivery_rate", nullable = false)
    private Double onTimeDeliveryRate;

    @Column(name = "avg_delivery_hours", nullable = false)
    private Double avgDeliveryHours;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
