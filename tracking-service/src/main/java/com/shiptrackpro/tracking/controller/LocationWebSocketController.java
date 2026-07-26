package com.shiptrackpro.tracking.controller;

import com.shiptrackpro.tracking.dto.request.LocationUpdateRequest;
import com.shiptrackpro.tracking.dto.response.LocationResponse;
import com.shiptrackpro.tracking.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Controller for handling incoming STOMP messages over WebSockets.
 * Drivers can publish location updates directly to /app/location.update.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class LocationWebSocketController {

    private final TrackingService trackingService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handles location updates sent via WebSocket STOMP.
     * Destination: /app/location.update
     */
    @MessageMapping("/location.update")
    public void handleLocationUpdate(@Payload LocationUpdateRequest request) {
        log.debug("Received WebSocket location update for shipment {}", request.getShipmentId());
        LocationResponse response = trackingService.recordLocation(request);
        // Note: trackingService.recordLocation already broadcasts to /topic/tracking/{shipmentId}
    }
}
