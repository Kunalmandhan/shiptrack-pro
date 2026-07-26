package com.shiptrackpro.shipment.controller;

import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.common.dto.ApiResponse;
import com.shiptrackpro.common.exception.ShipTrackException;
import com.shiptrackpro.shipment.dto.request.CreateVehicleRequest;
import com.shiptrackpro.shipment.dto.request.UpdateVehicleRequest;
import com.shiptrackpro.shipment.dto.response.VehicleResponse;
import com.shiptrackpro.shipment.service.VehicleService;
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
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle management (Admin only)")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @Operation(summary = "Register vehicle", description = "Admin-only: register a new vehicle")
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @Valid @RequestBody CreateVehicleRequest request) {
        requireAdmin(role);
        VehicleResponse vehicle = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle registered", vehicle, "/api/v1/vehicles"));
    }

    @GetMapping
    @Operation(summary = "List all vehicles", description = "Admin-only: paginated list of all vehicles")
    public ResponseEntity<ApiResponse<Page<VehicleResponse>>> getAllVehicles(
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir) {
        requireAdmin(role);
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE), sort);
        Page<VehicleResponse> vehicles = vehicleService.getAllVehicles(pageable);
        return ResponseEntity.ok(ApiResponse.success("Vehicles retrieved", vehicles, "/api/v1/vehicles"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID", description = "Admin-only: get vehicle details")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @PathVariable UUID id) {
        requireAdmin(role);
        VehicleResponse vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle found", vehicle, "/api/v1/vehicles/" + id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle", description = "Admin-only: update vehicle info")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVehicleRequest request) {
        requireAdmin(role);
        VehicleResponse vehicle = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated", vehicle, "/api/v1/vehicles/" + id));
    }

    @GetMapping("/available")
    @Operation(summary = "List available vehicles", description = "Admin-only: list vehicles currently available")
    public ResponseEntity<ApiResponse<Page<VehicleResponse>>> getAvailableVehicles(
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        requireAdmin(role);
        Pageable pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE));
        Page<VehicleResponse> vehicles = vehicleService.getAvailableVehicles(pageable);
        return ResponseEntity.ok(ApiResponse.success("Available vehicles", vehicles, "/api/v1/vehicles/available"));
    }

    private void requireAdmin(String role) {
        if (!AppConstants.ROLE_ADMIN.equalsIgnoreCase(role)) {
            throw new ShipTrackException("Admin access required", "ACCESS_DENIED", HttpStatus.FORBIDDEN);
        }
    }
}
