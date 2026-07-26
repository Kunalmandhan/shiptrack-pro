package com.shiptrackpro.tracking.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrackpro.tracking.dto.response.LocationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisLocationServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RedisLocationServiceImpl redisLocationService;

    private UUID shipmentId;
    private LocationResponse locationResponse;

    @BeforeEach
    void setUp() {
        shipmentId = UUID.randomUUID();
        locationResponse = LocationResponse.builder()
                .shipmentId(shipmentId)
                .latitude(37.7749)
                .longitude(-122.4194)
                .build();
    }

    @Test
    void saveShipmentLocation_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisLocationService.saveShipmentLocation(shipmentId, locationResponse);

        verify(valueOperations).set(eq("location:" + shipmentId), eq(locationResponse), any());
    }

    @Test
    void getShipmentLocation_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("location:" + shipmentId)).thenReturn(locationResponse);
        when(objectMapper.convertValue(locationResponse, LocationResponse.class)).thenReturn(locationResponse);

        Optional<LocationResponse> result = redisLocationService.getShipmentLocation(shipmentId);

        assertTrue(result.isPresent());
        assertEquals(shipmentId, result.get().getShipmentId());
    }

    @Test
    void getShipmentLocation_Null_ReturnsEmpty() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("location:" + shipmentId)).thenReturn(null);

        Optional<LocationResponse> result = redisLocationService.getShipmentLocation(shipmentId);

        assertTrue(result.isEmpty());
    }
}
