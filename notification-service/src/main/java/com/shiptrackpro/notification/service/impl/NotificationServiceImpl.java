package com.shiptrackpro.notification.service.impl;

import com.shiptrackpro.common.exception.ResourceNotFoundException;
import com.shiptrackpro.common.exception.ShipTrackException;
import com.shiptrackpro.notification.dto.request.SendNotificationRequest;
import com.shiptrackpro.notification.dto.response.NotificationResponse;
import com.shiptrackpro.notification.dto.response.UnreadCountResponse;
import com.shiptrackpro.notification.entity.Notification;
import com.shiptrackpro.notification.enums.NotificationChannel;
import com.shiptrackpro.notification.enums.NotificationStatus;
import com.shiptrackpro.notification.mapper.NotificationMapper;
import com.shiptrackpro.notification.repository.NotificationRepository;
import com.shiptrackpro.notification.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailNotificationService emailNotificationService;
    private final SmsNotificationService smsNotificationService;
    private final PushNotificationService pushNotificationService;

    @Override
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        Notification notification = notificationMapper.toNotification(request);
        notification.setStatus(NotificationStatus.PENDING);
        notification = notificationRepository.save(notification);

        boolean sentSuccess = false;

        if (request.getChannel() == NotificationChannel.EMAIL) {
            sentSuccess = emailNotificationService.sendEmail(request.getRecipientEmail(), request.getTitle(), request.getMessage());
        } else if (request.getChannel() == NotificationChannel.SMS) {
            sentSuccess = smsNotificationService.sendSms(request.getRecipientPhone(), request.getMessage());
        } else if (request.getChannel() == NotificationChannel.PUSH) {
            sentSuccess = pushNotificationService.sendPushNotification(request.getRecipientId(), request.getTitle(), request.getMessage());
        }

        notification.setStatus(sentSuccess ? NotificationStatus.SENT : NotificationStatus.FAILED);
        notification = notificationRepository.save(notification);

        log.info("Dispatched notification {} via {} with status {}", notification.getId(), request.getChannel(), notification.getStatus());
        return notificationMapper.toNotificationResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(UUID recipientId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable)
                .map(notificationMapper::toNotificationResponse);
    }

    @Override
    public NotificationResponse markAsRead(UUID notificationId, UUID recipientId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId.toString()));

        if (notification.getRecipientId() != null && !notification.getRecipientId().equals(recipientId)) {
            throw new ShipTrackException("Access denied to notification", "ACCESS_DENIED", HttpStatus.FORBIDDEN);
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return notificationMapper.toNotificationResponse(notification);
    }

    @Override
    public void markAllAsRead(UUID recipientId) {
        notificationRepository.markAllAsReadForRecipient(recipientId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(UUID recipientId) {
        long count = notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
        return UnreadCountResponse.builder().unreadCount(count).build();
    }
}
