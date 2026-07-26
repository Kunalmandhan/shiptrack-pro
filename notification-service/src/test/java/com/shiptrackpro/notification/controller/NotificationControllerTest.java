package com.shiptrackpro.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrackpro.common.constant.AppConstants;
import com.shiptrackpro.notification.dto.response.NotificationResponse;
import com.shiptrackpro.notification.dto.response.UnreadCountResponse;
import com.shiptrackpro.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    private UUID userId;
    private UUID notificationId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
    }

    @Test
    void getMyNotifications_Success() throws Exception {
        NotificationResponse resp = NotificationResponse.builder()
                .id(notificationId)
                .title("Test Notification")
                .build();

        when(notificationService.getMyNotifications(eq(userId), any())).thenReturn(new PageImpl<>(List.of(resp)));

        mockMvc.perform(get("/api/v1/notifications/my")
                        .header(AppConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Test Notification"));
    }

    @Test
    void markAsRead_Success() throws Exception {
        NotificationResponse resp = NotificationResponse.builder()
                .id(notificationId)
                .isRead(true)
                .build();

        when(notificationService.markAsRead(eq(notificationId), eq(userId))).thenReturn(resp);

        mockMvc.perform(put("/api/v1/notifications/{id}/read", notificationId)
                        .header(AppConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.read").value(true));
    }

    @Test
    void getUnreadCount_Success() throws Exception {
        UnreadCountResponse resp = UnreadCountResponse.builder().unreadCount(3L).build();

        when(notificationService.getUnreadCount(userId)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header(AppConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(3));
    }
}
