package com.shiptrackpro.shipment.controller;

import com.shiptrackpro.shipment.enums.ShipmentStatus;
import com.shiptrackpro.shipment.repository.DriverRepository;
import com.shiptrackpro.shipment.repository.ShipmentRepository;
import com.shiptrackpro.shipment.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalShipmentStatsController.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalShipmentStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShipmentRepository shipmentRepository;

    @MockitoBean
    private DriverRepository driverRepository;

    @MockitoBean
    private VehicleRepository vehicleRepository;

    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
    }

    @Test
    void getPlatformSummary_Success() throws Exception {
        when(shipmentRepository.count()).thenReturn(10L);
        when(shipmentRepository.countByStatus(ShipmentStatus.DELIVERED)).thenReturn(8L);
        when(shipmentRepository.countByStatus(ShipmentStatus.IN_TRANSIT)).thenReturn(2L);

        mockMvc.perform(get("/internal/shipments/stats/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalShipments").value(10))
                .andExpect(jsonPath("$.data.deliveredCount").value(8))
                .andExpect(jsonPath("$.data.onTimeDeliveryRate").value(80.0));
    }

    @Test
    void getCustomerSummary_Success() throws Exception {
        when(shipmentRepository.countBySenderId(eq(customerId))).thenReturn(5L);
        when(shipmentRepository.countBySenderIdAndStatus(eq(customerId), eq(ShipmentStatus.DELIVERED))).thenReturn(4L);

        mockMvc.perform(get("/internal/shipments/stats/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalShipments").value(5))
                .andExpect(jsonPath("$.data.deliveredCount").value(4))
                .andExpect(jsonPath("$.data.onTimeDeliveryRate").value(80.0));
    }
}
