package com.shiptrackpro.shipment.service;

import com.shiptrackpro.shipment.dto.request.CreateVehicleRequest;
import com.shiptrackpro.shipment.dto.request.UpdateVehicleRequest;
import com.shiptrackpro.shipment.dto.response.VehicleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VehicleService {

    VehicleResponse createVehicle(CreateVehicleRequest request);

    Page<VehicleResponse> getAllVehicles(Pageable pageable);

    VehicleResponse getVehicleById(UUID id);

    VehicleResponse updateVehicle(UUID id, UpdateVehicleRequest request);

    Page<VehicleResponse> getAvailableVehicles(Pageable pageable);
}
