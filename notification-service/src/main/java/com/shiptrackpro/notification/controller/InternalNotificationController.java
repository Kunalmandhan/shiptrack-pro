package com.shiptrackpro.notification.controller;

import com.shiptrackpro.common.dto.ApiResponse;
import com.shiptrackpro.notification.dto.request.SendNotificationRequest;
import com.shiptrackpro.notification.dto.response.NotificationResponse;
import com.shiptrackpro.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Internal Notifications", description = "Inter-service notification dispatch triggers")
public class InternalNotificationController {

    private final NotificationService notificationService;

    @PostMapping("/internal/notifications/send")
    @Operation(summary = "Internal send notification", description = "Inter-service trigger called by Shipment/Tracking services")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendInternalNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification dispatched", response, "/internal/notifications/send"));
    }

    @PostMapping("/api/v1/notifications/send")
    @Operation(summary = "Admin send notification", description = "Admin manual dispatch trigger")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendAdminNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification dispatched", response, "/api/v1/notifications/send"));
    }
}
