package com.shiptrackpro.notification.service;

import com.shiptrackpro.notification.dto.request.SendNotificationRequest;
import com.shiptrackpro.notification.dto.response.NotificationResponse;
import com.shiptrackpro.notification.dto.response.UnreadCountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    NotificationResponse sendNotification(SendNotificationRequest request);

    Page<NotificationResponse> getMyNotifications(UUID recipientId, Pageable pageable);

    NotificationResponse markAsRead(UUID notificationId, UUID recipientId);

    void markAllAsRead(UUID recipientId);

    UnreadCountResponse getUnreadCount(UUID recipientId);
}
