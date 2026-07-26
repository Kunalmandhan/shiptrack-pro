package com.shiptrackpro.analytics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrackpro.analytics.dto.request.ReportGenerateRequest;
import com.shiptrackpro.analytics.dto.response.ReportResponseDTO;
import com.shiptrackpro.analytics.enums.ReportStatus;
import com.shiptrackpro.analytics.enums.ReportType;
import com.shiptrackpro.analytics.service.ReportService;
import com.shiptrackpro.common.constant.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReportService reportService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void generateReport_Success() throws Exception {
        ReportGenerateRequest req = ReportGenerateRequest.builder()
                .reportType(ReportType.SHIPMENT_SUMMARY)
                .build();

        ReportResponseDTO resp = ReportResponseDTO.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .reportType(ReportType.SHIPMENT_SUMMARY)
                .status(ReportStatus.COMPLETED)
                .downloadUrl("/api/v1/analytics/reports/download/xyz.pdf")
                .generatedAt(LocalDateTime.now())
                .build();

        when(reportService.generateReport(eq(userId), any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/analytics/reports/generate")
                        .header(AppConstants.HEADER_USER_ID, userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportType").value("SHIPMENT_SUMMARY"));
    }

    @Test
    void getUserReports_Success() throws Exception {
        ReportResponseDTO resp = ReportResponseDTO.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .reportType(ReportType.DELAY_ANALYSIS)
                .status(ReportStatus.COMPLETED)
                .build();

        when(reportService.getUserReports(userId)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/v1/analytics/reports/my")
                        .header(AppConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].reportType").value("DELAY_ANALYSIS"));
    }
}
