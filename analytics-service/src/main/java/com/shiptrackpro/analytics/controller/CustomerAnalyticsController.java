package com.shiptrackpro.analytics.controller;

import com.shiptrackpro.analytics.dto.response.CustomerDashboardDTO;
import com.shiptrackpro.analytics.dto.response.ShipmentVolumeDataPointDTO;
import com.shiptrackpro.analytics.service.AnalyticsService;
import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics/customer")
@RequiredArgsConstructor
public class CustomerAnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<CustomerDashboardDTO>> getCustomerDashboard(
            @RequestHeader(value = AppConstants.HEADER_USER_ID, required = false) String userIdHeader) {

        if (userIdHeader == null || userIdHeader.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Missing authentication header", "/api/v1/analytics/customer/dashboard"));
        }

        UUID customerId = UUID.fromString(userIdHeader);
        CustomerDashboardDTO data = analyticsService.getCustomerDashboard(customerId);
        return ResponseEntity.ok(ApiResponse.success(data, "Customer dashboard metrics fetched successfully"));
    }

    @GetMapping("/volume")
    public ResponseEntity<ApiResponse<List<ShipmentVolumeDataPointDTO>>> getCustomerVolumeSeries(
            @RequestHeader(value = AppConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @RequestParam(value = "period", defaultValue = "14DAYS") String period) {

        if (userIdHeader == null || userIdHeader.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Missing authentication header", "/api/v1/analytics/customer/volume"));
        }

        UUID customerId = UUID.fromString(userIdHeader);
        List<ShipmentVolumeDataPointDTO> data = analyticsService.getCustomerVolumeSeries(customerId, period);
        return ResponseEntity.ok(ApiResponse.success(data, "Customer volume series fetched successfully"));
    }
}
