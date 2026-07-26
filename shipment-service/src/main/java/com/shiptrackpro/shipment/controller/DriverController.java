package com.shiptrackpro.shipment.controller;

import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.common.dto.ApiResponse;
import com.shiptrackpro.common.exception.ShipTrackException;
import com.shiptrackpro.shipment.dto.request.CreateDriverRequest;
import com.shiptrackpro.shipment.dto.request.UpdateDriverRequest;
import com.shiptrackpro.shipment.dto.response.DriverResponse;
import com.shiptrackpro.shipment.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Tag(name = "Drivers", description = "Driver management (Admin only)")
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    @Operation(summary = "Register driver", description = "Admin-only: register a new driver")
    public ResponseEntity<ApiResponse<DriverResponse>> createDriver(
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @Valid @RequestBody CreateDriverRequest request) {
        requireAdmin(role);
        DriverResponse driver = driverService.createDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Driver registered", driver, "/api/v1/drivers"));
    }

    @GetMapping
    @Operation(summary = "List all drivers", description = "Admin-only: paginated list of all drivers")
    public ResponseEntity<ApiResponse<Page<DriverResponse>>> getAllDrivers(
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir) {
        requireAdmin(role);
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE), sort);
        Page<DriverResponse> drivers = driverService.getAllDrivers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Drivers retrieved", drivers, "/api/v1/drivers"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get driver by ID", description = "Admin-only: get driver details")
    public ResponseEntity<ApiResponse<DriverResponse>> getDriverById(
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @PathVariable UUID id) {
        requireAdmin(role);
        DriverResponse driver = driverService.getDriverById(id);
        return ResponseEntity.ok(ApiResponse.success("Driver found", driver, "/api/v1/drivers/" + id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update driver", description = "Admin-only: update driver info")
    public ResponseEntity<ApiResponse<DriverResponse>> updateDriver(
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDriverRequest request) {
        requireAdmin(role);
        DriverResponse driver = driverService.updateDriver(id, request);
        return ResponseEntity.ok(ApiResponse.success("Driver updated", driver, "/api/v1/drivers/" + id));
    }

    @GetMapping("/available")
    @Operation(summary = "List available drivers", description = "Admin-only: list drivers currently available")
    public ResponseEntity<ApiResponse<Page<DriverResponse>>> getAvailableDrivers(
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        requireAdmin(role);
        Pageable pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE));
        Page<DriverResponse> drivers = driverService.getAvailableDrivers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Available drivers", drivers, "/api/v1/drivers/available"));
    }

    private void requireAdmin(String role) {
        if (!AppConstants.ROLE_ADMIN.equalsIgnoreCase(role)) {
            throw new ShipTrackException("Admin access required", "ACCESS_DENIED", HttpStatus.FORBIDDEN);
        }
    }
}
