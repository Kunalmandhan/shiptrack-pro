package com.shiptrackpro.analytics.controller;

import com.shiptrackpro.analytics.dto.response.*;
import com.shiptrackpro.analytics.service.AnalyticsService;
import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics/admin")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDTO>> getAdminDashboard(
            @RequestHeader(value = AppConstants.HEADER_USER_ROLE, required = false) String role) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Access denied: Admin role required", "/api/v1/analytics/admin/dashboard"));
        }

        AdminDashboardDTO data = analyticsService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success(data, "Admin dashboard metrics fetched successfully"));
    }

    @GetMapping("/shipments/volume")
    public ResponseEntity<ApiResponse<List<ShipmentVolumeDataPointDTO>>> getShipmentVolumeSeries(
            @RequestHeader(value = AppConstants.HEADER_USER_ROLE, required = false) String role,
            @RequestParam(value = "period", defaultValue = "14DAYS") String period) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Access denied: Admin role required", "/api/v1/analytics/admin/shipments/volume"));
        }

        List<ShipmentVolumeDataPointDTO> data = analyticsService.getAdminVolumeSeries(period);
        return ResponseEntity.ok(ApiResponse.success(data, "Shipment volume time-series fetched successfully"));
    }

    @GetMapping("/shipments/status-distribution")
    public ResponseEntity<ApiResponse<StatusDistributionDTO>> getStatusDistribution(
            @RequestHeader(value = AppConstants.HEADER_USER_ROLE, required = false) String role) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Access denied: Admin role required", "/api/v1/analytics/admin/shipments/status-distribution"));
        }

        StatusDistributionDTO data = analyticsService.getStatusDistribution();
        return ResponseEntity.ok(ApiResponse.success(data, "Status distribution fetched successfully"));
    }

    @GetMapping("/delays")
    public ResponseEntity<ApiResponse<DelayAnalysisDTO>> getDelayAnalysis(
            @RequestHeader(value = AppConstants.HEADER_USER_ROLE, required = false) String role) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Access denied: Admin role required", "/api/v1/analytics/admin/delays"));
        }

        DelayAnalysisDTO data = analyticsService.getDelayAnalysis();
        return ResponseEntity.ok(ApiResponse.success(data, "Delay analysis metrics fetched successfully"));
    }
}
