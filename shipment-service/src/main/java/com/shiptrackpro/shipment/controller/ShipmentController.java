package com.shiptrackpro.shipment.controller;

import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.common.dto.ApiResponse;
import com.shiptrackpro.common.exception.ShipTrackException;
import com.shiptrackpro.shipment.dto.request.AssignShipmentRequest;
import com.shiptrackpro.shipment.dto.request.CreateShipmentRequest;
import com.shiptrackpro.shipment.dto.request.UpdateStatusRequest;
import com.shiptrackpro.shipment.dto.response.ShipmentDetailResponse;
import com.shiptrackpro.shipment.dto.response.ShipmentResponse;
import com.shiptrackpro.shipment.dto.response.ShipmentTrackingResponse;
import com.shiptrackpro.shipment.service.ShipmentService;
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
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
@Tag(name = "Shipments", description = "Shipment CRUD, status management, and tracking")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    @Operation(summary = "Create shipment", description = "Create a new shipment (Customer or Admin)")
    public ResponseEntity<ApiResponse<ShipmentDetailResponse>> createShipment(
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId,
            @Valid @RequestBody CreateShipmentRequest request) {
        ShipmentDetailResponse shipment = shipmentService.createShipment(UUID.fromString(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Shipment created successfully", shipment, "/api/v1/shipments"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shipment by ID", description = "Get shipment details (Owner or Admin)")
    public ResponseEntity<ApiResponse<ShipmentDetailResponse>> getShipmentById(
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId,
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @PathVariable UUID id) {
        ShipmentDetailResponse shipment = shipmentService.getShipmentById(id, UUID.fromString(userId), role);
        return ResponseEntity.ok(ApiResponse.success("Shipment retrieved", shipment, "/api/v1/shipments/" + id));
    }

    @GetMapping
    @Operation(summary = "List all shipments (Admin)", description = "Admin-only: paginated list of all shipments")
    public ResponseEntity<ApiResponse<Page<ShipmentResponse>>> getAllShipments(
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir) {
        requireAdmin(role);
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE), sort);
        Page<ShipmentResponse> shipments = shipmentService.getAllShipments(pageable);
        return ResponseEntity.ok(ApiResponse.success("Shipments retrieved", shipments, "/api/v1/shipments"));
    }

    @GetMapping("/my")
    @Operation(summary = "List my shipments", description = "Customer: paginated list of own shipments")
    public ResponseEntity<ApiResponse<Page<ShipmentResponse>>> getMyShipments(
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE), sort);
        Page<ShipmentResponse> shipments = shipmentService.getMyShipments(UUID.fromString(userId), pageable);
        return ResponseEntity.ok(ApiResponse.success("My shipments retrieved", shipments, "/api/v1/shipments/my"));
    }

    @GetMapping("/track/{trackingNumber}")
    @Operation(summary = "Track shipment (Public)", description = "Public: track shipment by tracking number")
    public ResponseEntity<ApiResponse<ShipmentTrackingResponse>> trackShipment(
            @PathVariable String trackingNumber) {
        ShipmentTrackingResponse tracking = shipmentService.trackShipment(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Shipment tracking info", tracking,
                "/api/v1/shipments/track/" + trackingNumber));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update shipment status (Admin)", description = "Admin-only: update shipment status")
    public ResponseEntity<ApiResponse<ShipmentDetailResponse>> updateStatus(
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId,
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        requireAdmin(role);
        ShipmentDetailResponse shipment = shipmentService.updateStatus(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Shipment status updated", shipment,
                "/api/v1/shipments/" + id + "/status"));
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Assign driver & vehicle (Admin)", description = "Admin-only: assign driver and vehicle to shipment")
    public ResponseEntity<ApiResponse<ShipmentDetailResponse>> assignShipment(
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId,
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @PathVariable UUID id,
            @Valid @RequestBody AssignShipmentRequest request) {
        requireAdmin(role);
        ShipmentDetailResponse shipment = shipmentService.assignShipment(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Shipment assigned", shipment,
                "/api/v1/shipments/" + id + "/assign"));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel shipment", description = "Cancel shipment (Owner or Admin)")
    public ResponseEntity<ApiResponse<ShipmentDetailResponse>> cancelShipment(
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId,
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @PathVariable UUID id) {
        ShipmentDetailResponse shipment = shipmentService.cancelShipment(id, UUID.fromString(userId), role);
        return ResponseEntity.ok(ApiResponse.success("Shipment cancelled", shipment,
                "/api/v1/shipments/" + id + "/cancel"));
    }

    // ==================== Authorization Helper ====================

    private void requireAdmin(String role) {
        if (!AppConstants.ROLE_ADMIN.equalsIgnoreCase(role)) {
            throw new ShipTrackException("Admin access required", "ACCESS_DENIED", HttpStatus.FORBIDDEN);
        }
    }
}
