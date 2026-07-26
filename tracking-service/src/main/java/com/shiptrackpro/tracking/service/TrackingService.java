package com.shiptrackpro.tracking.service;

import com.shiptrackpro.tracking.dto.request.BatchLocationUpdateRequest;
import com.shiptrackpro.tracking.dto.request.LocationUpdateRequest;
import com.shiptrackpro.tracking.dto.response.LocationResponse;
import com.shiptrackpro.tracking.dto.response.TrackingHistoryResponse;

import java.util.UUID;

public interface TrackingService {

    LocationResponse recordLocation(LocationUpdateRequest request);

    void recordLocationBatch(BatchLocationUpdateRequest request);

    LocationResponse getLiveLocation(UUID shipmentId);

    TrackingHistoryResponse getTrackingHistory(UUID shipmentId);

    LocationResponse getDriverCurrentLocation(UUID driverId);
}
