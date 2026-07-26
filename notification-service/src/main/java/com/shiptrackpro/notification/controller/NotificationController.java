package com.shiptrackpro.notification.controller;

import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.common.dto.ApiResponse;
import com.shiptrackpro.notification.dto.response.NotificationResponse;
import com.shiptrackpro.notification.dto.response.UnreadCountResponse;
import com.shiptrackpro.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification center, inbox management, and read tracking")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my")
    @Operation(summary = "Get my notifications", description = "Fetch user's notifications inbox (paginated)")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, AppConstants.MAX_PAGE_SIZE), sort);
        Page<NotificationResponse> notifications = notificationService.getMyNotifications(UUID.fromString(userId), pageable);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", notifications, "/api/v1/notifications/my"));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a single notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId,
            @PathVariable UUID id) {
        NotificationResponse notification = notificationService.markAsRead(id, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification, "/api/v1/notifications/" + id + "/read"));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Mark all user notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId) {
        notificationService.markAllAsRead(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", "/api/v1/notifications/read-all"));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notifications count", description = "Get count of unread notifications for current user")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @RequestHeader(AppConstants.HEADER_USER_ID) String userId) {
        UnreadCountResponse count = notificationService.getUnreadCount(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved", count, "/api/v1/notifications/unread-count"));
    }
}
