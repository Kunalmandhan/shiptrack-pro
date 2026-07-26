package com.shiptrackpro.shipment.service;

import com.shiptrackpro.shipment.dto.request.CreateDriverRequest;
import com.shiptrackpro.shipment.dto.request.UpdateDriverRequest;
import com.shiptrackpro.shipment.dto.response.DriverResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DriverService {

    DriverResponse createDriver(CreateDriverRequest request);

    Page<DriverResponse> getAllDrivers(Pageable pageable);

    DriverResponse getDriverById(UUID id);

    DriverResponse updateDriver(UUID id, UpdateDriverRequest request);

    Page<DriverResponse> getAvailableDrivers(Pageable pageable);
}
