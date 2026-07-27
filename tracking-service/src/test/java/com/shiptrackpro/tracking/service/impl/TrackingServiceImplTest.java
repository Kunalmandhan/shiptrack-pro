package com.shiptrackpro.tracking.service.impl;

import com.shiptrackpro.common.exception.ResourceNotFoundException;
import com.shiptrackpro.tracking.dto.request.BatchLocationUpdateRequest;
import com.shiptrackpro.tracking.dto.request.LocationUpdateRequest;
import com.shiptrackpro.tracking.dto.response.LocationResponse;
import com.shiptrackpro.tracking.dto.response.TrackingHistoryResponse;
import com.shiptrackpro.tracking.entity.TrackingHistory;
import com.shiptrackpro.tracking.mapper.TrackingMapper;
import com.shiptrackpro.tracking.repository.TrackingHistoryRepository;
import com.shiptrackpro.tracking.service.RedisLocationService;
import com.shiptrackpro.tracking.util.DistanceCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingServiceImplTest {

    @Mock
    private TrackingHistoryRepository trackingHistoryRepository;

    @Mock
    private RedisLocationService redisLocationService;

    @Mock
    private TrackingMapper trackingMapper;

    @Mock
    private DistanceCalculator distanceCalculator;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TrackingServiceImpl trackingService;

    private UUID shipmentId;
    private UUID driverId;
    private LocationUpdateRequest updateRequest;
    private TrackingHistory history;
    private LocationResponse locationResponse;

    @BeforeEach
    void setUp() {
        shipmentId = UUID.randomUUID();
        driverId = UUID.randomUUID();

        updateRequest = LocationUpdateRequest.builder()
                .shipmentId(shipmentId)
                .driverId(driverId)
                .latitude(37.7749)
                .longitude(-122.4194)
                .recordedAt(LocalDateTime.now())
                .build();

        history = TrackingHistory.builder()
                .shipmentId(shipmentId)
                .driverId(driverId)
                .latitude(37.7749)
                .longitude(-122.4194)
                .recordedAt(LocalDateTime.now())
                .build();

        locationResponse = LocationResponse.builder()
                .shipmentId(shipmentId)
                .driverId(driverId)
                .latitude(37.7749)
                .longitude(-122.4194)
                .build();
    }

    @Test
    void recordLocation_Success() {
        when(trackingMapper.toTrackingHistory(updateRequest)).thenReturn(history);
        when(trackingHistoryRepository.save(history)).thenReturn(history);
        when(trackingMapper.toLocationResponse(history)).thenReturn(locationResponse);

        LocationResponse response = trackingService.recordLocation(updateRequest);

        assertNotNull(response);
        assertEquals(shipmentId, response.getShipmentId());
        verify(redisLocationService).saveShipmentLocation(eq(shipmentId), any());
        verify(redisLocationService).saveDriverLocation(eq(driverId), any());
        verify(messagingTemplate).convertAndSend(eq("/topic/tracking/" + shipmentId), eq(response));
    }

    @Test
    void getLiveLocation_FromRedisCache() {
        when(redisLocationService.getShipmentLocation(shipmentId)).thenReturn(Optional.of(locationResponse));

        LocationResponse response = trackingService.getLiveLocation(shipmentId);

        assertNotNull(response);
        assertEquals(shipmentId, response.getShipmentId());
        verifyNoInteractions(trackingHistoryRepository);
    }

    @Test
    void getLiveLocation_FallbackToDB() {
        when(redisLocationService.getShipmentLocation(shipmentId)).thenReturn(Optional.empty());
        when(trackingHistoryRepository.findFirstByShipmentIdOrderByRecordedAtDesc(shipmentId)).thenReturn(Optional.of(history));
        when(trackingMapper.toLocationResponse(history)).thenReturn(locationResponse);

        LocationResponse response = trackingService.getLiveLocation(shipmentId);

        assertNotNull(response);
        assertEquals(shipmentId, response.getShipmentId());
        verify(redisLocationService).saveShipmentLocation(shipmentId, locationResponse);
    }

    @Test
    void getLiveLocation_NotFound_ThrowsException() {
        when(redisLocationService.getShipmentLocation(shipmentId)).thenReturn(Optional.empty());
        when(trackingHistoryRepository.findFirstByShipmentIdOrderByRecordedAtDesc(shipmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> trackingService.getLiveLocation(shipmentId));
    }

    @Test
    void getTrackingHistory_Success() {
        TrackingHistory h1 = TrackingHistory.builder().latitude(37.7749).longitude(-122.4194).recordedAt(LocalDateTime.now().minusMinutes(10)).build();
        TrackingHistory h2 = TrackingHistory.builder().latitude(37.7849).longitude(-122.4094).recordedAt(LocalDateTime.now()).build();
        List<TrackingHistory> historyList = List.of(h1, h2);

        when(trackingHistoryRepository.findByShipmentIdOrderByRecordedAtAsc(shipmentId)).thenReturn(historyList);
        when(trackingMapper.toLocationResponseList(historyList)).thenReturn(List.of(locationResponse, locationResponse));
        when(distanceCalculator.calculateDistanceKm(37.7749, -122.4194, 37.7849, -122.4094)).thenReturn(1.5);

        TrackingHistoryResponse response = trackingService.getTrackingHistory(shipmentId);

        assertNotNull(response);
        assertEquals(shipmentId, response.getShipmentId());
        assertEquals(1.5, response.getTotalDistanceKm());
    }

    @Test
    void recordLocationBatch_Success() {
        BatchLocationUpdateRequest batchRequest = BatchLocationUpdateRequest.builder()
                .updates(List.of(updateRequest))
                .build();

        when(trackingMapper.toTrackingHistory(updateRequest)).thenReturn(history);
        when(trackingMapper.toLocationResponse(updateRequest)).thenReturn(locationResponse);

        trackingService.recordLocationBatch(batchRequest);

        verify(trackingHistoryRepository).saveAll(any());
        verify(redisLocationService).saveShipmentLocation(shipmentId, locationResponse);
    }
}
