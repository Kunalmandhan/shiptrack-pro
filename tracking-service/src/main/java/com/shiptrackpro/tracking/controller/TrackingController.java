package com.shiptrackpro.tracking.controller;

import com.shiptrackpro.common.dto.ApiResponse;
import com.shiptrackpro.tracking.dto.request.BatchLocationUpdateRequest;
import com.shiptrackpro.tracking.dto.request.LocationUpdateRequest;
import com.shiptrackpro.tracking.dto.response.LocationResponse;
import com.shiptrackpro.tracking.dto.response.TrackingHistoryResponse;
import com.shiptrackpro.tracking.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
@Tag(name = "Tracking", description = "Real-time location updates, live position queries, and historical breadcrumbs")
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/location")
    @Operation(summary = "Push location update", description = "Driver/System pushes a single GPS location update")
    public ResponseEntity<ApiResponse<LocationResponse>> recordLocation(
            @Valid @RequestBody LocationUpdateRequest request) {
        LocationResponse response = trackingService.recordLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Location update recorded", response, "/api/v1/tracking/location"));
    }

    @PostMapping("/location/batch")
    @Operation(summary = "Batch location updates", description = "Bulk push offline location updates")
    public ResponseEntity<ApiResponse<Void>> recordLocationBatch(
            @Valid @RequestBody BatchLocationUpdateRequest request) {
        trackingService.recordLocationBatch(request);
        return ResponseEntity.ok(ApiResponse.success("Batch location updates processed", "/api/v1/tracking/location/batch"));
    }

    @GetMapping("/{shipmentId}/live")
    @Operation(summary = "Get live location", description = "Fetch current real-time position of a shipment (from Redis/DB)")
    public ResponseEntity<ApiResponse<LocationResponse>> getLiveLocation(
            @PathVariable UUID shipmentId) {
        LocationResponse response = trackingService.getLiveLocation(shipmentId);
        return ResponseEntity.ok(ApiResponse.success("Live location retrieved", response, "/api/v1/tracking/" + shipmentId + "/live"));
    }

    @GetMapping("/{shipmentId}/history")
    @Operation(summary = "Get tracking history", description = "Fetch historical GPS breadcrumbs and total distance traveled")
    public ResponseEntity<ApiResponse<TrackingHistoryResponse>> getTrackingHistory(
            @PathVariable UUID shipmentId) {
        TrackingHistoryResponse response = trackingService.getTrackingHistory(shipmentId);
        return ResponseEntity.ok(ApiResponse.success("Tracking history retrieved", response, "/api/v1/tracking/" + shipmentId + "/history"));
    }

    @GetMapping("/driver/{driverId}/current")
    @Operation(summary = "Get driver current location", description = "Fetch current location of a specific driver")
    public ResponseEntity<ApiResponse<LocationResponse>> getDriverCurrentLocation(
            @PathVariable UUID driverId) {
        LocationResponse response = trackingService.getDriverCurrentLocation(driverId);
        return ResponseEntity.ok(ApiResponse.success("Driver current location retrieved", response, "/api/v1/tracking/driver/" + driverId + "/current"));
    }
}
