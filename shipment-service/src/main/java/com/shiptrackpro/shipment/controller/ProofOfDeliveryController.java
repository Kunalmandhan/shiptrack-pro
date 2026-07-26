package com.shiptrackpro.shipment.controller;

import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.common.dto.ApiResponse;
import com.shiptrackpro.shipment.dto.response.ProofOfDeliveryResponse;
import com.shiptrackpro.shipment.service.ProofOfDeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pod")
@RequiredArgsConstructor
@Tag(name = "Proof of Delivery", description = "POD upload, retrieval, and download")
public class ProofOfDeliveryController {

    private final ProofOfDeliveryService podService;

    @PostMapping(value = "/{shipmentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload Proof of Delivery", description = "Upload signature, photo, and recipient info (Admin)")
    public ResponseEntity<ApiResponse<ProofOfDeliveryResponse>> uploadPod(
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId,
            @PathVariable UUID shipmentId,
            @RequestParam("receivedBy") String receivedBy,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "signature", required = false) MultipartFile signature,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        ProofOfDeliveryResponse pod = podService.uploadPod(shipmentId, receivedBy, notes, signature, photo, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("POD uploaded successfully", pod, "/api/v1/pod/" + shipmentId));
    }

    @GetMapping("/{shipmentId}")
    @Operation(summary = "Get Proof of Delivery", description = "Get POD details for a shipment (Owner or Admin)")
    public ResponseEntity<ApiResponse<ProofOfDeliveryResponse>> getPod(
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId,
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @PathVariable UUID shipmentId) {
        ProofOfDeliveryResponse pod = podService.getPod(shipmentId, UUID.fromString(userId), role);
        return ResponseEntity.ok(ApiResponse.success("POD retrieved", pod, "/api/v1/pod/" + shipmentId));
    }

    @GetMapping("/{shipmentId}/download")
    @Operation(summary = "Download POD Photo", description = "Download POD photo image file (Owner or Admin)")
    public ResponseEntity<byte[]> downloadPodPhoto(
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId,
            @RequestHeader(AppConstants.HEADER_USER_ROLE) String role,
            @PathVariable UUID shipmentId) {
        byte[] imageBytes = podService.downloadPodPhoto(shipmentId, UUID.fromString(userId), role);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pod_" + shipmentId + ".jpg\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageBytes);
    }
}
