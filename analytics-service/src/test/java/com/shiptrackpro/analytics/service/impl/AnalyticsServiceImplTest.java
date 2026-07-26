package com.shiptrackpro.analytics.service.impl;

import com.shiptrackpro.analytics.client.ShipmentServiceClient;
import com.shiptrackpro.analytics.dto.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private ShipmentServiceClient shipmentServiceClient;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getAdminDashboard_CacheMiss_Success() {
        when(valueOperations.get(anyString())).thenReturn(null);

        ShipmentStatsResponse mockStats = ShipmentStatsResponse.builder()
                .totalShipments(20L)
                .createdCount(5L)
                .inTransitCount(5L)
                .deliveredCount(10L)
                .delayedCount(2L)
                .cancelledCount(0L)
                .onTimeDeliveryRate(80.0)
                .averageDeliveryHours(24.0)
                .statusDistribution(Map.of("DELIVERED", 10L, "IN_TRANSIT", 5L, "CREATED", 5L))
                .build();

        when(shipmentServiceClient.getPlatformStats()).thenReturn(mockStats);

        AdminDashboardDTO result = analyticsService.getAdminDashboard();

        assertNotNull(result);
        assertEquals(20L, result.getTotalShipments());
        assertEquals(10L, result.getActiveShipments());
        assertEquals(10L, result.getDeliveredShipments());
        assertEquals(80.0, result.getOnTimeDeliveryRate());
        verify(shipmentServiceClient, times(1)).getPlatformStats();
    }

    @Test
    void getCustomerDashboard_CacheMiss_Success() {
        when(valueOperations.get(anyString())).thenReturn(null);

        ShipmentStatsResponse mockStats = ShipmentStatsResponse.builder()
                .totalShipments(5L)
                .createdCount(1L)
                .inTransitCount(1L)
                .deliveredCount(3L)
                .delayedCount(0L)
                .onTimeDeliveryRate(100.0)
                .statusDistribution(Map.of("DELIVERED", 3L))
                .build();

        when(shipmentServiceClient.getCustomerStats(customerId)).thenReturn(mockStats);

        CustomerDashboardDTO result = analyticsService.getCustomerDashboard(customerId);

        assertNotNull(result);
        assertEquals(5L, result.getTotalShipments());
        assertEquals(2L, result.getActiveShipments());
        assertEquals(3L, result.getDeliveredShipments());
        verify(shipmentServiceClient, times(1)).getCustomerStats(customerId);
    }

    @Test
    void getAdminVolumeSeries_Success() {
        List<ShipmentVolumeDataPointDTO> series = analyticsService.getAdminVolumeSeries("14DAYS");
        assertNotNull(series);
        assertEquals(14, series.size());
    }

    @Test
    void getStatusDistribution_Success() {
        ShipmentStatsResponse mockStats = ShipmentStatsResponse.builder()
                .totalShipments(10L)
                .statusDistribution(Map.of("DELIVERED", 8L, "IN_TRANSIT", 2L))
                .build();

        when(shipmentServiceClient.getPlatformStats()).thenReturn(mockStats);

        StatusDistributionDTO result = analyticsService.getStatusDistribution();

        assertNotNull(result);
        assertEquals(10L, result.getTotal());
        assertEquals(80.0, result.getPercentageDistribution().get("DELIVERED"));
    }

    @Test
    void getDelayAnalysis_Success() {
        ShipmentStatsResponse mockStats = ShipmentStatsResponse.builder()
                .totalShipments(100L)
                .delayedCount(10L)
                .build();

        when(shipmentServiceClient.getPlatformStats()).thenReturn(mockStats);

        DelayAnalysisDTO result = analyticsService.getDelayAnalysis();

        assertNotNull(result);
        assertEquals(10L, result.getTotalDelayed());
        assertEquals(10.0, result.getDelayPercentage());
    }
}
