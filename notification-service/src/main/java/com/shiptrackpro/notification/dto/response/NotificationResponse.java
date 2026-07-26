package com.shiptrackpro.notification.dto.response;

import com.shiptrackpro.notification.enums.NotificationChannel;
import com.shiptrackpro.notification.enums.NotificationStatus;
import com.shiptrackpro.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private UUID recipientId;
    private String recipientEmail;
    private String recipientPhone;
    private NotificationChannel channel;
    private NotificationType type;
    private String title;
    private String message;
    private NotificationStatus status;
    private boolean isRead;
    private UUID referenceId;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
