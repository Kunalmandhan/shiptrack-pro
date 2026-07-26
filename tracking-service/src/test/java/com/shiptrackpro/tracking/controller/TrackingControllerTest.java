package com.shiptrackpro.tracking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrackpro.tracking.dto.request.LocationUpdateRequest;
import com.shiptrackpro.tracking.dto.response.LocationResponse;
import com.shiptrackpro.tracking.dto.response.TrackingHistoryResponse;
import com.shiptrackpro.tracking.service.TrackingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrackingController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrackingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrackingService trackingService;

    private UUID shipmentId;

    @BeforeEach
    void setUp() {
        shipmentId = UUID.randomUUID();
    }

    @Test
    void recordLocation_Success() throws Exception {
        LocationUpdateRequest req = LocationUpdateRequest.builder()
                .shipmentId(shipmentId)
                .latitude(37.7749)
                .longitude(-122.4194)
                .build();

        LocationResponse resp = LocationResponse.builder()
                .shipmentId(shipmentId)
                .latitude(37.7749)
                .longitude(-122.4194)
                .build();

        when(trackingService.recordLocation(any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/tracking/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shipmentId").value(shipmentId.toString()));
    }

    @Test
    void getLiveLocation_Success() throws Exception {
        LocationResponse resp = LocationResponse.builder()
                .shipmentId(shipmentId)
                .latitude(37.7749)
                .longitude(-122.4194)
                .build();

        when(trackingService.getLiveLocation(shipmentId)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/tracking/{shipmentId}/live", shipmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.latitude").value(37.7749));
    }

    @Test
    void getTrackingHistory_Success() throws Exception {
        TrackingHistoryResponse resp = TrackingHistoryResponse.builder()
                .shipmentId(shipmentId)
                .breadcrumbs(List.of())
                .totalDistanceKm(10.5)
                .build();

        when(trackingService.getTrackingHistory(shipmentId)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/tracking/{shipmentId}/history", shipmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalDistanceKm").value(10.5));
    }
}
