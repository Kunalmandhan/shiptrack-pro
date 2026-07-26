package com.shiptrackpro.notification.mapper;

import com.shiptrackpro.notification.dto.request.SendNotificationRequest;
import com.shiptrackpro.notification.dto.response.NotificationResponse;
import com.shiptrackpro.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toNotificationResponse(Notification notification);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isRead", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Notification toNotification(SendNotificationRequest request);

    List<NotificationResponse> toNotificationResponseList(List<Notification> notifications);
}
