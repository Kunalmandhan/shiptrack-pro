package com.shiptrackpro.shipment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.shipment.dto.request.CreateVehicleRequest;
import com.shiptrackpro.shipment.dto.response.VehicleResponse;
import com.shiptrackpro.shipment.enums.VehicleType;
import com.shiptrackpro.shipment.service.VehicleService;
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

@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VehicleService vehicleService;

    private UUID vehicleId;

    @BeforeEach
    void setUp() {
        vehicleId = UUID.randomUUID();
    }

    @Test
    void createVehicle_Admin_Success() throws Exception {
        CreateVehicleRequest req = CreateVehicleRequest.builder()
                .plateNumber("ABC-1234")
                .type(VehicleType.VAN)
                .model("Ford Transit")
                .capacityKg(1500.0)
                .build();

        VehicleResponse resp = VehicleResponse.builder()
                .id(vehicleId)
                .plateNumber("ABC-1234")
                .build();

        when(vehicleService.createVehicle(any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/vehicles")
                        .header(AppConstants.HEADER_USER_ROLE, "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.plateNumber").value("ABC-1234"));
    }

    @Test
    void createVehicle_Forbidden_Customer() throws Exception {
        CreateVehicleRequest req = CreateVehicleRequest.builder()
                .plateNumber("ABC-1234")
                .type(VehicleType.VAN)
                .model("Ford Transit")
                .capacityKg(1500.0)
                .build();

        mockMvc.perform(post("/api/v1/vehicles")
                        .header(AppConstants.HEADER_USER_ROLE, "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
