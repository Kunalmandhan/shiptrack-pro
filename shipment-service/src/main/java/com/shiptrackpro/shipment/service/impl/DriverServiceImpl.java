package com.shiptrackpro.shipment.service.impl;

import com.shiptrackpro.common.exception.DuplicateResourceException;
import com.shiptrackpro.common.exception.ResourceNotFoundException;
import com.shiptrackpro.shipment.dto.request.CreateDriverRequest;
import com.shiptrackpro.shipment.dto.request.UpdateDriverRequest;
import com.shiptrackpro.shipment.dto.response.DriverResponse;
import com.shiptrackpro.shipment.entity.Driver;
import com.shiptrackpro.shipment.mapper.ShipmentMapper;
import com.shiptrackpro.shipment.repository.DriverRepository;
import com.shiptrackpro.shipment.service.DriverService;
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
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final ShipmentMapper shipmentMapper;

    @Override
    public DriverResponse createDriver(CreateDriverRequest request) {
        if (driverRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Driver", "email", request.getEmail());
        }
        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicateResourceException("Driver", "licenseNumber", request.getLicenseNumber());
        }

        Driver driver = Driver.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .licenseNumber(request.getLicenseNumber())
                .available(true)
                .build();

        driver = driverRepository.save(driver);
        log.info("Driver created: {} ({})", driver.getName(), driver.getEmail());
        return shipmentMapper.toDriverResponse(driver);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DriverResponse> getAllDrivers(Pageable pageable) {
        return driverRepository.findAll(pageable)
                .map(shipmentMapper::toDriverResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse getDriverById(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id.toString()));
        return shipmentMapper.toDriverResponse(driver);
    }

    @Override
    public DriverResponse updateDriver(UUID id, UpdateDriverRequest request) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id.toString()));

        if (request.getName() != null) {
            driver.setName(request.getName());
        }
        if (request.getPhone() != null) {
            driver.setPhone(request.getPhone());
        }
        if (request.getAvailable() != null) {
            driver.setAvailable(request.getAvailable());
        }

        driver = driverRepository.save(driver);
        log.info("Driver updated: {} ({})", driver.getName(), driver.getId());
        return shipmentMapper.toDriverResponse(driver);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DriverResponse> getAvailableDrivers(Pageable pageable) {
        return driverRepository.findByAvailableTrue(pageable)
                .map(shipmentMapper::toDriverResponse);
    }
}
