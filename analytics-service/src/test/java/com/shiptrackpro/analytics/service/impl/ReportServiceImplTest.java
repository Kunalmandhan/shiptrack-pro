package com.shiptrackpro.analytics.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrackpro.analytics.dto.request.ReportGenerateRequest;
import com.shiptrackpro.analytics.dto.response.ReportResponseDTO;
import com.shiptrackpro.analytics.entity.ReportExport;
import com.shiptrackpro.analytics.enums.ReportStatus;
import com.shiptrackpro.analytics.enums.ReportType;
import com.shiptrackpro.analytics.repository.ReportExportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportExportRepository reportExportRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ReportServiceImpl reportService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void generateReport_Success() {
        ReportGenerateRequest req = ReportGenerateRequest.builder()
                .reportType(ReportType.SHIPMENT_SUMMARY)
                .build();

        ReportExport savedEntity = ReportExport.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .reportType(ReportType.SHIPMENT_SUMMARY)
                .status(ReportStatus.COMPLETED)
                .downloadUrl("/api/v1/analytics/reports/download/123.pdf")
                .generatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        when(reportExportRepository.save(any())).thenReturn(savedEntity);

        ReportResponseDTO res = reportService.generateReport(userId, req);

        assertNotNull(res);
        assertEquals(ReportType.SHIPMENT_SUMMARY, res.getReportType());
        assertEquals(ReportStatus.COMPLETED, res.getStatus());
    }

    @Test
    void getUserReports_Success() {
        ReportExport entity = ReportExport.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .reportType(ReportType.DELAY_ANALYSIS)
                .status(ReportStatus.COMPLETED)
                .generatedAt(LocalDateTime.now())
                .build();

        when(reportExportRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(entity));

        List<ReportResponseDTO> reports = reportService.getUserReports(userId);

        assertNotNull(reports);
        assertEquals(1, reports.size());
        assertEquals(ReportType.DELAY_ANALYSIS, reports.get(0).getReportType());
    }
}
