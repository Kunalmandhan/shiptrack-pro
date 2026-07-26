package com.shiptrackpro.notification.service.impl;

import com.shiptrackpro.common.exception.ResourceNotFoundException;
import com.shiptrackpro.common.exception.ShipTrackException;
import com.shiptrackpro.notification.dto.request.SendNotificationRequest;
import com.shiptrackpro.notification.dto.response.NotificationResponse;
import com.shiptrackpro.notification.dto.response.UnreadCountResponse;
import com.shiptrackpro.notification.entity.Notification;
import com.shiptrackpro.notification.enums.NotificationChannel;
import com.shiptrackpro.notification.enums.NotificationStatus;
import com.shiptrackpro.notification.enums.NotificationType;
import com.shiptrackpro.notification.mapper.NotificationMapper;
import com.shiptrackpro.notification.repository.NotificationRepository;
import com.shiptrackpro.notification.service.EmailNotificationService;
import com.shiptrackpro.notification.service.PushNotificationService;
import com.shiptrackpro.notification.service.SmsNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private SmsNotificationService smsNotificationService;

    @Mock
    private PushNotificationService pushNotificationService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UUID recipientId;
    private UUID notificationId;
    private Notification notification;
    private SendNotificationRequest sendRequest;
    private NotificationResponse notificationResponse;

    @BeforeEach
    void setUp() {
        recipientId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        notification = Notification.builder()
                .recipientId(recipientId)
                .recipientEmail("user@example.com")
                .channel(NotificationChannel.EMAIL)
                .type(NotificationType.SHIPMENT_CREATED)
                .title("Shipment Created")
                .message("Your shipment has been created")
                .status(NotificationStatus.PENDING)
                .isRead(false)
                .build();
        notification.setId(notificationId);

        sendRequest = SendNotificationRequest.builder()
                .recipientId(recipientId)
                .recipientEmail("user@example.com")
                .channel(NotificationChannel.EMAIL)
                .type(NotificationType.SHIPMENT_CREATED)
                .title("Shipment Created")
                .message("Your shipment has been created")
                .build();

        notificationResponse = NotificationResponse.builder()
                .id(notificationId)
                .recipientId(recipientId)
                .channel(NotificationChannel.EMAIL)
                .status(NotificationStatus.SENT)
                .build();
    }

    @Test
    void sendNotification_EmailSuccess() {
        when(notificationMapper.toNotification(sendRequest)).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(emailNotificationService.sendEmail("user@example.com", "Shipment Created", "Your shipment has been created")).thenReturn(true);
        when(notificationMapper.toNotificationResponse(any(Notification.class))).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.sendNotification(sendRequest);

        assertNotNull(result);
        assertEquals(NotificationStatus.SENT, notification.getStatus());
        verify(emailNotificationService).sendEmail(any(), any(), any());
    }

    @Test
    void getMyNotifications_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification));
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable)).thenReturn(page);
        when(notificationMapper.toNotificationResponse(notification)).thenReturn(notificationResponse);

        Page<NotificationResponse> result = notificationService.getMyNotifications(recipientId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);
        when(notificationMapper.toNotificationResponse(notification)).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.markAsRead(notificationId, recipientId);

        assertNotNull(result);
        assertTrue(notification.isRead());
        assertNotNull(notification.getReadAt());
    }

    @Test
    void markAsRead_AccessDenied_ThrowsException() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        assertThrows(ShipTrackException.class, () -> notificationService.markAsRead(notificationId, UUID.randomUUID()));
    }

    @Test
    void markAsRead_NotFound_ThrowsException() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(notificationId, recipientId));
    }

    @Test
    void getUnreadCount_Success() {
        when(notificationRepository.countByRecipientIdAndIsReadFalse(recipientId)).thenReturn(5L);

        UnreadCountResponse result = notificationService.getUnreadCount(recipientId);

        assertNotNull(result);
        assertEquals(5L, result.getUnreadCount());
    }
}
