package com.shiptrackpro.tracking.service.impl;

import com.shiptrackpro.common.exception.ResourceNotFoundException;
import com.shiptrackpro.tracking.dto.request.BatchLocationUpdateRequest;
import com.shiptrackpro.tracking.dto.request.LocationUpdateRequest;
import com.shiptrackpro.tracking.dto.response.LocationResponse;
import com.shiptrackpro.tracking.dto.response.TrackingHistoryResponse;
import com.shiptrackpro.tracking.entity.TrackingHistory;
import com.shiptrackpro.tracking.dto.TrackingMapper;
import com.shiptrackpro.tracking.repository.TrackingHistoryRepository;
import com.shiptrackpro.tracking.service.RedisLocationService;
import com.shiptrackpro.tracking.service.TrackingService;
import com.shiptrackpro.tracking.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrackingServiceImpl implements TrackingService {

    private final TrackingHistoryRepository trackingHistoryRepository;
    private final RedisLocationService redisLocationService;
    private final TrackingMapper trackingMapper;
    private final DistanceCalculator distanceCalculator;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public LocationResponse recordLocation(LocationUpdateRequest request) {
        if (request.getRecordedAt() == null) {
            request.setRecordedAt(LocalDateTime.now());
        }

        // 1. Save to PostgreSQL database
        TrackingHistory entity = trackingMapper.toTrackingHistory(request);
        entity = trackingHistoryRepository.save(entity);

        LocationResponse response = trackingMapper.toLocationResponse(entity);

        // 2. Cache in Redis for fast lookup
        redisLocationService.saveShipmentLocation(request.getShipmentId(), response);
        if (request.getDriverId() != null) {
            redisLocationService.saveDriverLocation(request.getDriverId(), response);
        }

        // 3. Broadcast to WebSocket STOMP topic (/topic/tracking/{shipmentId})
        try {
            messagingTemplate.convertAndSend("/topic/tracking/" + request.getShipmentId(), response);
            if (request.getDriverId() != null) {
                messagingTemplate.convertAndSend("/topic/driver/" + request.getDriverId(), response);
            }
        } catch (Exception e) {
            log.warn("Failed to broadcast location update over WebSocket: {}", e.getMessage());
        }

        log.debug("Recorded location for shipment {}: [{}, {}]", request.getShipmentId(), request.getLatitude(), request.getLongitude());
        return response;
    }

    @Override
    public void recordLocationBatch(BatchLocationUpdateRequest request) {
        if (request.getUpdates() == null || request.getUpdates().isEmpty()) {
            return;
        }

        List<TrackingHistory> entities = new ArrayList<>();
        for (LocationUpdateRequest update : request.getUpdates()) {
            if (update.getRecordedAt() == null) {
                update.setRecordedAt(LocalDateTime.now());
            }
            entities.add(trackingMapper.toTrackingHistory(update));
        }

        // Batch save to DB
        trackingHistoryRepository.saveAll(entities);

        // Update Redis cache with the latest point from batch
        LocationUpdateRequest lastUpdate = request.getUpdates().get(request.getUpdates().size() - 1);
        LocationResponse response = trackingMapper.toLocationResponse(lastUpdate);
        redisLocationService.saveShipmentLocation(lastUpdate.getShipmentId(), response);
        if (lastUpdate.getDriverId() != null) {
            redisLocationService.saveDriverLocation(lastUpdate.getDriverId(), response);
        }

        log.info("Batch recorded {} location pings for shipment {}", request.getUpdates().size(), lastUpdate.getShipmentId());
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse getLiveLocation(UUID shipmentId) {
        // 1. Try Redis first (sub-millisecond)
        Optional<LocationResponse> cached = redisLocationService.getShipmentLocation(shipmentId);
        if (cached.isPresent()) {
            return cached.get();
        }

        // 2. Fallback to PostgreSQL DB
        TrackingHistory history = trackingHistoryRepository.findFirstByShipmentIdOrderByRecordedAtDesc(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("TrackingHistory", "shipmentId", shipmentId.toString()));

        LocationResponse response = trackingMapper.toLocationResponse(history);
        redisLocationService.saveShipmentLocation(shipmentId, response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingHistoryResponse getTrackingHistory(UUID shipmentId) {
        List<TrackingHistory> historyList = trackingHistoryRepository.findByShipmentIdOrderByRecordedAtAsc(shipmentId);

        if (historyList.isEmpty()) {
            throw new ResourceNotFoundException("TrackingHistory", "shipmentId", shipmentId.toString());
        }

        List<LocationResponse> breadcrumbs = trackingMapper.toLocationResponseList(historyList);

        // Calculate total distance traveled along the breadcrumbs
        double totalDistance = 0.0;
        for (int i = 1; i < historyList.size(); i++) {
            TrackingHistory prev = historyList.get(i - 1);
            TrackingHistory curr = historyList.get(i);
            totalDistance += distanceCalculator.calculateDistanceKm(
                    prev.getLatitude(), prev.getLongitude(),
                    curr.getLatitude(), curr.getLongitude()
            );
        }

        TrackingHistory last = historyList.get(historyList.size() - 1);

        return TrackingHistoryResponse.builder()
                .shipmentId(shipmentId)
                .driverId(last.getDriverId())
                .breadcrumbs(breadcrumbs)
                .totalDistanceKm(Math.round(totalDistance * 100.0) / 100.0)
                .lastRecordedAt(last.getRecordedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse getDriverCurrentLocation(UUID driverId) {
        // 1. Try Redis first
        Optional<LocationResponse> cached = redisLocationService.getDriverLocation(driverId);
        if (cached.isPresent()) {
            return cached.get();
        }

        // 2. Fallback to DB
        TrackingHistory history = trackingHistoryRepository.findFirstByDriverIdOrderByRecordedAtDesc(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("TrackingHistory", "driverId", driverId.toString()));

        LocationResponse response = trackingMapper.toLocationResponse(history);
        redisLocationService.saveDriverLocation(driverId, response);
        return response;
    }
}
