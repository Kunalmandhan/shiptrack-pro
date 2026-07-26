package com.shiptrackpro.tracking.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.tracking.dto.response.LocationResponse;
import com.shiptrackpro.tracking.service.RedisLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLocationServiceImpl implements RedisLocationService {

    private static final String KEY_SHIPMENT_PREFIX = "location:";
    private static final String KEY_DRIVER_PREFIX = "location:driver:";
    private static final Duration TTL_24_HOURS = Duration.ofHours(24);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void saveShipmentLocation(UUID shipmentId, LocationResponse location) {
        String key = KEY_SHIPMENT_PREFIX + shipmentId;
        try {
            redisTemplate.opsForValue().set(key, location, TTL_24_HOURS);
        } catch (Exception e) {
            log.error("Failed to save shipment location in Redis for shipment {}: {}", shipmentId, e.getMessage());
        }
    }

    @Override
    public Optional<LocationResponse> getShipmentLocation(UUID shipmentId) {
        String key = KEY_SHIPMENT_PREFIX + shipmentId;
        try {
            Object raw = redisTemplate.opsForValue().get(key);
            if (raw != null) {
                LocationResponse response = objectMapper.convertValue(raw, LocationResponse.class);
                return Optional.of(response);
            }
        } catch (Exception e) {
            log.error("Failed to read shipment location from Redis for shipment {}: {}", shipmentId, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void saveDriverLocation(UUID driverId, LocationResponse location) {
        String key = KEY_DRIVER_PREFIX + driverId;
        try {
            redisTemplate.opsForValue().set(key, location, TTL_24_HOURS);
        } catch (Exception e) {
            log.error("Failed to save driver location in Redis for driver {}: {}", driverId, e.getMessage());
        }
    }

    @Override
    public Optional<LocationResponse> getDriverLocation(UUID driverId) {
        String key = KEY_DRIVER_PREFIX + driverId;
        try {
            Object raw = redisTemplate.opsForValue().get(key);
            if (raw != null) {
                LocationResponse response = objectMapper.convertValue(raw, LocationResponse.class);
                return Optional.of(response);
            }
        } catch (Exception e) {
            log.error("Failed to read driver location from Redis for driver {}: {}", driverId, e.getMessage());
        }
        return Optional.empty();
    }
}
