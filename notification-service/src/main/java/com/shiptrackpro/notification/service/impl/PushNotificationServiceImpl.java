package com.shiptrackpro.notification.service.impl;

import com.shiptrackpro.notification.service.PushNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class PushNotificationServiceImpl implements PushNotificationService {

    @Override
    public boolean sendPushNotification(UUID recipientId, String title, String message) {
        if (recipientId == null) {
            log.warn("Cannot send Push: recipientId is null");
            return false;
        }

        // Firebase Cloud Messaging (FCM) / Push provider integration stub
        log.info("[Push Stub] Sent Push notification to user {}: [{}] {}", recipientId, title, message);
        return true;
    }
}
