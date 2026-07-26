package com.shiptrackpro.notification.dto.request;

import com.shiptrackpro.notification.enums.NotificationChannel;
import com.shiptrackpro.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    private UUID recipientId;

    private String recipientEmail;

    private String recipientPhone;

    @NotNull(message = "Notification channel is required")
    private NotificationChannel channel;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private UUID referenceId;
}
