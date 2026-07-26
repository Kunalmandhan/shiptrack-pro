package com.shiptrackpro.shipment.service;

import com.shiptrackpro.shipment.dto.request.AssignShipmentRequest;
import com.shiptrackpro.shipment.dto.request.CreateShipmentRequest;
import com.shiptrackpro.shipment.dto.request.UpdateStatusRequest;
import com.shiptrackpro.shipment.dto.response.ShipmentDetailResponse;
import com.shiptrackpro.shipment.dto.response.ShipmentResponse;
import com.shiptrackpro.shipment.dto.response.ShipmentTrackingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ShipmentService {

    ShipmentDetailResponse createShipment(UUID senderId, CreateShipmentRequest request);

    ShipmentDetailResponse getShipmentById(UUID id, UUID userId, String role);

    Page<ShipmentResponse> getAllShipments(Pageable pageable);

    Page<ShipmentResponse> getMyShipments(UUID senderId, Pageable pageable);

    ShipmentTrackingResponse trackShipment(String trackingNumber);

    ShipmentDetailResponse updateStatus(UUID id, UpdateStatusRequest request, String userId);

    ShipmentDetailResponse assignShipment(UUID id, AssignShipmentRequest request, String userId);

    ShipmentDetailResponse cancelShipment(UUID id, UUID userId, String role);
}
