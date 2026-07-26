package com.shiptrackpro.analytics.service;

import com.shiptrackpro.analytics.dto.request.ReportGenerateRequest;
import com.shiptrackpro.analytics.dto.response.ReportResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ReportService {

    ReportResponseDTO generateReport(UUID userId, ReportGenerateRequest request);

    List<ReportResponseDTO> getUserReports(UUID userId);
}
