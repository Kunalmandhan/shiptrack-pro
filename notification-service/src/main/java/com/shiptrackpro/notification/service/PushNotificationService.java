package com.shiptrackpro.notification.service;

import java.util.UUID;

public interface PushNotificationService {
    boolean sendPushNotification(UUID recipientId, String title, String message);
}
