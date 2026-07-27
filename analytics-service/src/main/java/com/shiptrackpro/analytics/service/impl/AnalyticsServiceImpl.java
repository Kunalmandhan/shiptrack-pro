package com.shiptrackpro.analytics.service.impl;

import com.shiptrackpro.analytics.client.ShipmentServiceClient;
import com.shiptrackpro.analytics.dto.response.*;
import com.shiptrackpro.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ShipmentServiceClient shipmentServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_ADMIN_DASHBOARD = "analytics:admin:dashboard";
    private static final String REDIS_KEY_CUSTOMER_DASHBOARD_PREFIX = "analytics:customer:";
    private static final long CACHE_TTL_MINUTES = 5;

    @Override
    public AdminDashboardDTO getAdminDashboard() {
        try {
            Object cached = redisTemplate.opsForValue().get(REDIS_KEY_ADMIN_DASHBOARD);
            if (cached instanceof AdminDashboardDTO dto) {
                log.info("Returning cached admin dashboard metrics");
                return dto;
            }
        } catch (Exception e) {
            log.warn("Redis cache read failed: {}", e.getMessage());
        }

        ShipmentStatsResponse stats = shipmentServiceClient.getPlatformStats();
        long active = stats.getCreatedCount() + stats.getPickedUpCount() + stats.getInTransitCount() + stats.getOutForDeliveryCount();

        AdminDashboardDTO dto = AdminDashboardDTO.builder()
                .totalShipments(stats.getTotalShipments())
                .activeShipments(active)
                .deliveredShipments(stats.getDeliveredCount())
                .delayedShipments(stats.getDelayedCount())
                .cancelledShipments(stats.getCancelledCount())
                .onTimeDeliveryRate(stats.getOnTimeDeliveryRate())
                .avgDeliveryHours(stats.getAverageDeliveryHours())
                .statusDistribution(stats.getStatusDistribution() != null ? stats.getStatusDistribution() : Map.of())
                .build();

        try {
            redisTemplate.opsForValue().set(REDIS_KEY_ADMIN_DASHBOARD, dto, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis cache write failed: {}", e.getMessage());
        }

        return dto;
    }

    @Override
    public CustomerDashboardDTO getCustomerDashboard(UUID customerId) {
        String redisKey = REDIS_KEY_CUSTOMER_DASHBOARD_PREFIX + customerId;
        try {
            Object cached = redisTemplate.opsForValue().get(redisKey);
            if (cached instanceof CustomerDashboardDTO dto) {
                log.info("Returning cached customer dashboard metrics for {}", customerId);
                return dto;
            }
        } catch (Exception e) {
            log.warn("Redis cache read failed: {}", e.getMessage());
        }

        ShipmentStatsResponse stats = shipmentServiceClient.getCustomerStats(customerId);
        long active = stats.getCreatedCount() + stats.getPickedUpCount() + stats.getInTransitCount() + stats.getOutForDeliveryCount();

        CustomerDashboardDTO dto = CustomerDashboardDTO.builder()
                .totalShipments(stats.getTotalShipments())
                .activeShipments(active)
                .deliveredShipments(stats.getDeliveredCount())
                .delayedShipments(stats.getDelayedCount())
                .onTimeDeliveryRate(stats.getOnTimeDeliveryRate())
                .statusDistribution(stats.getStatusDistribution() != null ? stats.getStatusDistribution() : Map.of())
                .build();

        try {
            redisTemplate.opsForValue().set(redisKey, dto, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis cache write failed: {}", e.getMessage());
        }

        return dto;
    }

    @Override
    public List<ShipmentVolumeDataPointDTO> getAdminVolumeSeries(String period) {
        return generateVolumeSeries(period, 1.0);
    }

    @Override
    public List<ShipmentVolumeDataPointDTO> getCustomerVolumeSeries(UUID customerId, String period) {
        return generateVolumeSeries(period, 0.4);
    }

    @Override
    public StatusDistributionDTO getStatusDistribution() {
        ShipmentStatsResponse stats = shipmentServiceClient.getPlatformStats();
        Map<String, Long> dist = stats.getStatusDistribution() != null ? stats.getStatusDistribution() : new HashMap<>();
        long total = stats.getTotalShipments();

        Map<String, Double> percentages = new HashMap<>();
        if (total > 0) {
            dist.forEach((key, val) -> {
                double pct = ((double) val / total) * 100.0;
                percentages.put(key, Math.round(pct * 100.0) / 100.0);
            });
        }

        return StatusDistributionDTO.builder()
                .total(total)
                .distribution(dist)
                .percentageDistribution(percentages)
                .build();
    }

    /**
     * PLACEHOLDER: Returns fabricated delay analysis data.
     * TODO: Replace with real delay tracking data from shipment status transitions
     *       and a delay_reasons table when delay tracking is implemented.
     */
    @Override
    public DelayAnalysisDTO getDelayAnalysis() {
        ShipmentStatsResponse stats = shipmentServiceClient.getPlatformStats();
        long delayed = stats.getDelayedCount();
        long total = stats.getTotalShipments();
        double pct = total > 0 ? ((double) delayed / total) * 100.0 : 0.0;

        Map<String, Long> reasons = new HashMap<>();
        reasons.put("TRAFFIC_CONGESTION", Math.round(delayed * 0.4));
        reasons.put("WEATHER_DISRUPTION", Math.round(delayed * 0.3));
        reasons.put("VEHICLE_BREAKDOWN", Math.round(delayed * 0.2));
        reasons.put("ADDRESS_ISSUE", Math.max(0, delayed - Math.round(delayed * 0.9)));

        return DelayAnalysisDTO.builder()
                .totalDelayed(delayed)
                .delayPercentage(Math.round(pct * 100.0) / 100.0)
                .avgDelayHours(3.5)
                .delayReasons(reasons)
                .build();
    }

    /**
     * PLACEHOLDER: Generates synthetic shipment volume time-series data.
     * TODO: Replace with real aggregated shipment counts from shipment-service
     *       using a daily/weekly rollup query.
     */
    private List<ShipmentVolumeDataPointDTO> generateVolumeSeries(String period, double multiplier) {
        List<ShipmentVolumeDataPointDTO> list = new ArrayList<>();
        int days = "MONTH".equalsIgnoreCase(period) ? 30 : ("WEEK".equalsIgnoreCase(period) ? 7 : 14);
        LocalDate now = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = now.minusDays(i);
            String label = date.format(DateTimeFormatter.ofPattern("MMM dd"));
            long total = Math.round((15 + (i % 5) * 4) * multiplier);
            long delivered = Math.round(total * 0.8);
            long delayed = total - delivered;

            list.add(ShipmentVolumeDataPointDTO.builder()
                    .label(label)
                    .total(total)
                    .delivered(delivered)
                    .delayed(delayed)
                    .build());
        }
        return list;
    }
}
