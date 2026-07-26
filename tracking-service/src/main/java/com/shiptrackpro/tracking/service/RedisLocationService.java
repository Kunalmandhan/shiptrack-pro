package com.shiptrackpro.tracking.service;

import com.shiptrackpro.tracking.dto.response.LocationResponse;

import java.util.Optional;
import java.util.UUID;

public interface RedisLocationService {

    void saveShipmentLocation(UUID shipmentId, LocationResponse location);

    Optional<LocationResponse> getShipmentLocation(UUID shipmentId);

    void saveDriverLocation(UUID driverId, LocationResponse location);

    Optional<LocationResponse> getDriverLocation(UUID driverId);
}
