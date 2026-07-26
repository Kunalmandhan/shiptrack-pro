package com.shiptrackpro.shipment.service.impl;

import com.shiptrackpro.common.exception.DuplicateResourceException;
import com.shiptrackpro.common.exception.ResourceNotFoundException;
import com.shiptrackpro.shipment.dto.request.CreateVehicleRequest;
import com.shiptrackpro.shipment.dto.request.UpdateVehicleRequest;
import com.shiptrackpro.shipment.dto.response.VehicleResponse;
import com.shiptrackpro.shipment.entity.Vehicle;
import com.shiptrackpro.shipment.enums.VehicleType;
import com.shiptrackpro.shipment.mapper.ShipmentMapper;
import com.shiptrackpro.shipment.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ShipmentMapper shipmentMapper;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private UUID vehicleId;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicleId = UUID.randomUUID();
        vehicle = Vehicle.builder()
                .plateNumber("ABC-1234")
                .type(VehicleType.VAN)
                .model("Ford Transit")
                .capacityKg(1500.0)
                .available(true)
                .build();
        vehicle.setId(vehicleId);
    }

    @Test
    void createVehicle_Success() {
        CreateVehicleRequest req = CreateVehicleRequest.builder()
                .plateNumber("ABC-1234")
                .type(VehicleType.VAN)
                .model("Ford Transit")
                .capacityKg(1500.0)
                .build();

        when(vehicleRepository.existsByPlateNumber("ABC-1234")).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);
        when(shipmentMapper.toVehicleResponse(vehicle))
                .thenReturn(VehicleResponse.builder().id(vehicleId).plateNumber("ABC-1234").build());

        VehicleResponse resp = vehicleService.createVehicle(req);

        assertNotNull(resp);
        assertEquals("ABC-1234", resp.getPlateNumber());
    }

    @Test
    void createVehicle_DuplicatePlate_ThrowsException() {
        CreateVehicleRequest req = CreateVehicleRequest.builder()
                .plateNumber("ABC-1234")
                .build();

        when(vehicleRepository.existsByPlateNumber("ABC-1234")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> vehicleService.createVehicle(req));
    }

    @Test
    void getVehicleById_Success() {
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(shipmentMapper.toVehicleResponse(vehicle))
                .thenReturn(VehicleResponse.builder().id(vehicleId).build());

        VehicleResponse resp = vehicleService.getVehicleById(vehicleId);

        assertNotNull(resp);
        assertEquals(vehicleId, resp.getId());
    }

    @Test
    void getVehicleById_NotFound() {
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.getVehicleById(vehicleId));
    }
}
