package com.shiptrackpro.shipment.service.impl;

import com.shiptrackpro.common.exception.DuplicateResourceException;
import com.shiptrackpro.common.exception.ResourceNotFoundException;
import com.shiptrackpro.shipment.dto.request.CreateVehicleRequest;
import com.shiptrackpro.shipment.dto.request.UpdateVehicleRequest;
import com.shiptrackpro.shipment.dto.response.VehicleResponse;
import com.shiptrackpro.shipment.entity.Vehicle;
import com.shiptrackpro.shipment.mapper.ShipmentMapper;
import com.shiptrackpro.shipment.repository.VehicleRepository;
import com.shiptrackpro.shipment.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ShipmentMapper shipmentMapper;

    @Override
    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        if (vehicleRepository.existsByPlateNumber(request.getPlateNumber())) {
            throw new DuplicateResourceException("Vehicle", "plateNumber", request.getPlateNumber());
        }

        Vehicle vehicle = Vehicle.builder()
                .plateNumber(request.getPlateNumber())
                .type(request.getType())
                .model(request.getModel())
                .capacityKg(request.getCapacityKg())
                .available(true)
                .build();

        vehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle created: {} ({})", vehicle.getPlateNumber(), vehicle.getType());
        return shipmentMapper.toVehicleResponse(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> getAllVehicles(Pageable pageable) {
        return vehicleRepository.findAll(pageable)
                .map(shipmentMapper::toVehicleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id.toString()));
        return shipmentMapper.toVehicleResponse(vehicle);
    }

    @Override
    public VehicleResponse updateVehicle(UUID id, UpdateVehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id.toString()));

        if (request.getModel() != null) {
            vehicle.setModel(request.getModel());
        }
        if (request.getCapacityKg() != null) {
            vehicle.setCapacityKg(request.getCapacityKg());
        }
        if (request.getAvailable() != null) {
            vehicle.setAvailable(request.getAvailable());
        }

        vehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle updated: {} ({})", vehicle.getPlateNumber(), vehicle.getId());
        return shipmentMapper.toVehicleResponse(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> getAvailableVehicles(Pageable pageable) {
        return vehicleRepository.findByAvailableTrue(pageable)
                .map(shipmentMapper::toVehicleResponse);
    }
}
