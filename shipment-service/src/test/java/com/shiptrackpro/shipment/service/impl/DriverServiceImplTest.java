package com.shiptrackpro.shipment.service.impl;

import com.shiptrackpro.common.exception.DuplicateResourceException;
import com.shiptrackpro.common.exception.ResourceNotFoundException;
import com.shiptrackpro.shipment.dto.request.CreateDriverRequest;
import com.shiptrackpro.shipment.dto.request.UpdateDriverRequest;
import com.shiptrackpro.shipment.dto.response.DriverResponse;
import com.shiptrackpro.shipment.entity.Driver;
import com.shiptrackpro.shipment.mapper.ShipmentMapper;
import com.shiptrackpro.shipment.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private ShipmentMapper shipmentMapper;

    @InjectMocks
    private DriverServiceImpl driverService;

    private UUID driverId;
    private Driver driver;

    @BeforeEach
    void setUp() {
        driverId = UUID.randomUUID();
        driver = Driver.builder()
                .name("John Driver")
                .email("driver@example.com")
                .phone("1234567890")
                .licenseNumber("LIC-12345")
                .available(true)
                .build();
        driver.setId(driverId);
    }

    @Test
    void createDriver_Success() {
        CreateDriverRequest req = CreateDriverRequest.builder()
                .name("John Driver")
                .email("driver@example.com")
                .phone("1234567890")
                .licenseNumber("LIC-12345")
                .build();

        when(driverRepository.existsByEmail("driver@example.com")).thenReturn(false);
        when(driverRepository.existsByLicenseNumber("LIC-12345")).thenReturn(false);
        when(driverRepository.save(any())).thenReturn(driver);
        when(shipmentMapper.toDriverResponse(driver))
                .thenReturn(DriverResponse.builder().id(driverId).name("John Driver").build());

        DriverResponse resp = driverService.createDriver(req);

        assertNotNull(resp);
        assertEquals("John Driver", resp.getName());
    }

    @Test
    void createDriver_DuplicateEmail_ThrowsException() {
        CreateDriverRequest req = CreateDriverRequest.builder()
                .email("driver@example.com")
                .build();

        when(driverRepository.existsByEmail("driver@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> driverService.createDriver(req));
    }

    @Test
    void getDriverById_Success() {
        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(shipmentMapper.toDriverResponse(driver))
                .thenReturn(DriverResponse.builder().id(driverId).build());

        DriverResponse resp = driverService.getDriverById(driverId);

        assertNotNull(resp);
        assertEquals(driverId, resp.getId());
    }

    @Test
    void getDriverById_NotFound() {
        when(driverRepository.findById(driverId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> driverService.getDriverById(driverId));
    }

    @Test
    void updateDriver_Success() {
        UpdateDriverRequest req = UpdateDriverRequest.builder()
                .name("John Updated")
                .available(false)
                .build();

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(driverRepository.save(driver)).thenReturn(driver);
        when(shipmentMapper.toDriverResponse(driver))
                .thenReturn(DriverResponse.builder().id(driverId).name("John Updated").available(false).build());

        DriverResponse resp = driverService.updateDriver(driverId, req);

        assertNotNull(resp);
        assertEquals("John Updated", resp.getName());
        assertFalse(resp.isAvailable());
    }
}
