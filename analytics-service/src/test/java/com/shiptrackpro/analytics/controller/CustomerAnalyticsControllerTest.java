package com.shiptrackpro.analytics.controller;

import com.shiptrackpro.analytics.dto.response.CustomerDashboardDTO;

import com.shiptrackpro.analytics.service.AnalyticsService;
import com.shiptrackpro.common.constant.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerAnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
    }

    @Test
    void getCustomerDashboard_Success() throws Exception {
        CustomerDashboardDTO dto = CustomerDashboardDTO.builder()
                .totalShipments(8L)
                .activeShipments(3L)
                .deliveredShipments(5L)
                .onTimeDeliveryRate(100.0)
                .statusDistribution(Map.of("DELIVERED", 5L))
                .build();

        when(analyticsService.getCustomerDashboard(customerId)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/analytics/customer/dashboard")
                        .header(AppConstants.HEADER_USER_ID, customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalShipments").value(8));
    }

    @Test
    void getCustomerDashboard_Unauthorized_MissingHeader() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/customer/dashboard"))
                .andExpect(status().isUnauthorized());
    }
}
