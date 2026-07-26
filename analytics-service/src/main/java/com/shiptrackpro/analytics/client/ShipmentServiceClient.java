package com.shiptrackpro.analytics.client;

import com.shiptrackpro.analytics.dto.response.ShipmentStatsResponse;
import com.shiptrackpro.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.shipment-service-url:http://localhost:8083}")
    private String shipmentServiceUrl;

    public ShipmentStatsResponse getPlatformStats() {
        String url = shipmentServiceUrl + "/internal/shipments/stats/summary";
        try {
            ResponseEntity<ApiResponse<ShipmentStatsResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<ShipmentStatsResponse>>() {}
            );
            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getData();
            }
        } catch (Exception e) {
            log.error("Failed to fetch platform stats from Shipment Service at {}: {}", url, e.getMessage());
        }
        return getFallbackStats();
    }

    public ShipmentStatsResponse getCustomerStats(UUID customerId) {
        String url = shipmentServiceUrl + "/internal/shipments/stats/customer/" + customerId;
        try {
            ResponseEntity<ApiResponse<ShipmentStatsResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<ShipmentStatsResponse>>() {}
            );
            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getData();
            }
        } catch (Exception e) {
            log.error("Failed to fetch customer stats for {} from Shipment Service at {}: {}", customerId, url, e.getMessage());
        }
        return getFallbackStats();
    }

    private ShipmentStatsResponse getFallbackStats() {
        return ShipmentStatsResponse.builder()
                .totalShipments(0)
                .createdCount(0)
                .pickedUpCount(0)
                .inTransitCount(0)
                .outForDeliveryCount(0)
                .deliveredCount(0)
                .failedCount(0)
                .cancelledCount(0)
                .delayedCount(0)
                .onTimeDeliveryRate(0.0)
                .averageDeliveryHours(0.0)
                .statusDistribution(new HashMap<>())
                .build();
    }
}
