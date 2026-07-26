package com.shiptrackpro.analytics.controller;

import com.shiptrackpro.analytics.dto.request.ReportGenerateRequest;
import com.shiptrackpro.analytics.dto.response.ReportResponseDTO;
import com.shiptrackpro.analytics.service.ReportService;
import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ReportResponseDTO>> generateReport(
            @RequestHeader(value = AppConstants.HEADER_USER_ID, required = false) String userIdHeader,
            @Valid @RequestBody ReportGenerateRequest request) {

        if (userIdHeader == null || userIdHeader.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Missing authentication header", "/api/v1/analytics/reports/generate"));
        }

        UUID userId = UUID.fromString(userIdHeader);
        ReportResponseDTO response = reportService.generateReport(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Report export request submitted successfully"));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReportResponseDTO>>> getUserReports(
            @RequestHeader(value = AppConstants.HEADER_USER_ID, required = false) String userIdHeader) {

        if (userIdHeader == null || userIdHeader.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Missing authentication header", "/api/v1/analytics/reports/my"));
        }

        UUID userId = UUID.fromString(userIdHeader);
        List<ReportResponseDTO> reports = reportService.getUserReports(userId);
        return ResponseEntity.ok(ApiResponse.success(reports, "User reports retrieved successfully"));
    }
}
