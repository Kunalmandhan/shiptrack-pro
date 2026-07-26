package com.shiptrackpro.shipment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.shipment.dto.request.CreateDriverRequest;
import com.shiptrackpro.shipment.dto.response.DriverResponse;
import com.shiptrackpro.shipment.service.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DriverController.class)
@AutoConfigureMockMvc(addFilters = false)
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DriverService driverService;

    private UUID driverId;

    @BeforeEach
    void setUp() {
        driverId = UUID.randomUUID();
    }

    @Test
    void createDriver_Admin_Success() throws Exception {
        CreateDriverRequest req = CreateDriverRequest.builder()
                .name("John Driver")
                .email("driver@example.com")
                .phone("1234567890")
                .licenseNumber("LIC-123")
                .build();

        DriverResponse resp = DriverResponse.builder()
                .id(driverId)
                .name("John Driver")
                .build();

        when(driverService.createDriver(any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/drivers")
                        .header(AppConstants.HEADER_USER_ROLE, "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("John Driver"));
    }

    @Test
    void createDriver_Forbidden_Customer() throws Exception {
        CreateDriverRequest req = CreateDriverRequest.builder()
                .name("John Driver")
                .email("driver@example.com")
                .phone("1234567890")
                .licenseNumber("LIC-123")
                .build();

        mockMvc.perform(post("/api/v1/drivers")
                        .header(AppConstants.HEADER_USER_ROLE, "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
