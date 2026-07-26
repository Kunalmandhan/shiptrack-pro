package com.shiptrackpro.analytics.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrackpro.analytics.dto.request.ReportGenerateRequest;
import com.shiptrackpro.analytics.dto.response.ReportResponseDTO;
import com.shiptrackpro.analytics.entity.ReportExport;
import com.shiptrackpro.analytics.enums.ReportStatus;
import com.shiptrackpro.analytics.repository.ReportExportRepository;
import com.shiptrackpro.analytics.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportExportRepository reportExportRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ReportResponseDTO generateReport(UUID userId, ReportGenerateRequest request) {
        String paramsJson = "{}";
        try {
            if (request.getParameters() != null) {
                paramsJson = objectMapper.writeValueAsString(request.getParameters());
            }
        } catch (Exception e) {
            log.warn("Failed to serialize report parameters: {}", e.getMessage());
        }

        UUID reportId = UUID.randomUUID();
        String mockDownloadUrl = "/api/v1/analytics/reports/download/" + reportId + ".pdf";

        ReportExport export = ReportExport.builder()
                .id(reportId)
                .userId(userId)
                .reportType(request.getReportType())
                .parameters(paramsJson)
                .status(ReportStatus.COMPLETED)
                .downloadUrl(mockDownloadUrl)
                .generatedAt(LocalDateTime.now())
                .build();

        ReportExport saved = reportExportRepository.save(export);
        log.info("Report export generated successfully for user {}, type {}", userId, request.getReportType());

        return mapToDTO(saved);
    }

    @Override
    public List<ReportResponseDTO> getUserReports(UUID userId) {
        return reportExportRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private ReportResponseDTO mapToDTO(ReportExport entity) {
        return ReportResponseDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .reportType(entity.getReportType())
                .status(entity.getStatus())
                .downloadUrl(entity.getDownloadUrl())
                .generatedAt(entity.getGeneratedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
