package com.shiptrackpro.shipment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.shipment.dto.request.AssignShipmentRequest;
import com.shiptrackpro.shipment.dto.request.CreateShipmentRequest;
import com.shiptrackpro.shipment.dto.request.UpdateStatusRequest;
import com.shiptrackpro.shipment.dto.response.ShipmentDetailResponse;
import com.shiptrackpro.shipment.dto.response.ShipmentResponse;
import com.shiptrackpro.shipment.dto.response.ShipmentTrackingResponse;
import com.shiptrackpro.shipment.enums.PackageType;
import com.shiptrackpro.shipment.enums.ShipmentStatus;
import com.shiptrackpro.shipment.service.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShipmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShipmentService shipmentService;

    private UUID userId;
    private UUID shipmentId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        shipmentId = UUID.randomUUID();
    }

    @Test
    void createShipment_Success() throws Exception {
        CreateShipmentRequest req = CreateShipmentRequest.builder()
                .senderName("John Doe")
                .senderEmail("john@example.com")
                .senderPhone("1234567890")
                .originAddress("123 Origin St")
                .receiverName("Jane Smith")
                .receiverEmail("jane@example.com")
                .receiverPhone("0987654321")
                .destinationAddress("456 Dest St")
                .weightKg(5.0)
                .packageType(PackageType.PARCEL)
                .build();

        ShipmentDetailResponse resp = ShipmentDetailResponse.builder()
                .id(shipmentId)
                .trackingNumber("STP-123456")
                .status(ShipmentStatus.CREATED)
                .build();

        when(shipmentService.createShipment(eq(userId), any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/shipments")
                        .header(AppConstants.HEADER_USER_ID, userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trackingNumber").value("STP-123456"));
    }

    @Test
    void getShipmentById_Success() throws Exception {
        ShipmentDetailResponse resp = ShipmentDetailResponse.builder()
                .id(shipmentId)
                .trackingNumber("STP-123456")
                .build();

        when(shipmentService.getShipmentById(eq(shipmentId), eq(userId), eq("CUSTOMER"))).thenReturn(resp);

        mockMvc.perform(get("/api/v1/shipments/{id}", shipmentId)
                        .header(AppConstants.HEADER_USER_ID, userId.toString())
                        .header(AppConstants.HEADER_USER_ROLE, "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(shipmentId.toString()));
    }

    @Test
    void trackShipment_Public_Success() throws Exception {
        ShipmentTrackingResponse resp = ShipmentTrackingResponse.builder()
                .trackingNumber("STP-123456")
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        when(shipmentService.trackShipment("STP-123456")).thenReturn(resp);

        mockMvc.perform(get("/api/v1/shipments/track/{trackingNumber}", "STP-123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trackingNumber").value("STP-123456"));
    }

    @Test
    void getAllShipments_Admin_Success() throws Exception {
        when(shipmentService.getAllShipments(any())).thenReturn(new PageImpl<>(List.of(ShipmentResponse.builder().build())));

        mockMvc.perform(get("/api/v1/shipments")
                        .header(AppConstants.HEADER_USER_ROLE, "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getAllShipments_Forbidden_Customer() throws Exception {
        mockMvc.perform(get("/api/v1/shipments")
                        .header(AppConstants.HEADER_USER_ROLE, "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignShipment_Admin_Success() throws Exception {
        AssignShipmentRequest req = AssignShipmentRequest.builder()
                .driverId(UUID.randomUUID())
                .vehicleId(UUID.randomUUID())
                .build();

        ShipmentDetailResponse resp = ShipmentDetailResponse.builder()
                .id(shipmentId)
                .status(ShipmentStatus.PROCESSING)
                .build();

        when(shipmentService.assignShipment(eq(shipmentId), any(), eq(userId.toString()))).thenReturn(resp);

        mockMvc.perform(put("/api/v1/shipments/{id}/assign", shipmentId)
                        .header(AppConstants.HEADER_USER_ID, userId.toString())
                        .header(AppConstants.HEADER_USER_ROLE, "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
