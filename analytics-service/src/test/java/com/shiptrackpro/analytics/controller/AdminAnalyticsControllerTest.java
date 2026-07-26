package com.shiptrackpro.analytics.controller;

import com.shiptrackpro.analytics.dto.response.*;
import com.shiptrackpro.analytics.service.AnalyticsService;
import com.shiptrackpro.common.constant.AppConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @Test
    void getAdminDashboard_Admin_Success() throws Exception {
        AdminDashboardDTO dto = AdminDashboardDTO.builder()
                .totalShipments(50L)
                .activeShipments(20L)
                .deliveredShipments(30L)
                .onTimeDeliveryRate(90.0)
                .statusDistribution(Map.of("DELIVERED", 30L))
                .build();

        when(analyticsService.getAdminDashboard()).thenReturn(dto);

        mockMvc.perform(get("/api/v1/analytics/admin/dashboard")
                        .header(AppConstants.HEADER_USER_ROLE, "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalShipments").value(50))
                .andExpect(jsonPath("$.data.onTimeDeliveryRate").value(90.0));
    }

    @Test
    void getAdminDashboard_Forbidden_Customer() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/admin/dashboard")
                        .header(AppConstants.HEADER_USER_ROLE, "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getShipmentVolumeSeries_Admin_Success() throws Exception {
        ShipmentVolumeDataPointDTO dp = ShipmentVolumeDataPointDTO.builder()
                .label("Jul 26")
                .total(15L)
                .delivered(12L)
                .delayed(3L)
                .build();

        when(analyticsService.getAdminVolumeSeries("14DAYS")).thenReturn(List.of(dp));

        mockMvc.perform(get("/api/v1/analytics/admin/shipments/volume")
                        .header(AppConstants.HEADER_USER_ROLE, "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].label").value("Jul 26"));
    }
}
