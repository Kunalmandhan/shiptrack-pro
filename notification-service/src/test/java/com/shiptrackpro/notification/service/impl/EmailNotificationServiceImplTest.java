package com.shiptrackpro.notification.service.impl;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailNotificationServiceImpl emailNotificationService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void sendEmail_Success() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        boolean result = emailNotificationService.sendEmail("recipient@example.com", "Test Subject", "<p>Test Content</p>");

        assertTrue(result);
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_EmptyRecipient_ReturnsFalse() {
        boolean result = emailNotificationService.sendEmail("", "Test Subject", "<p>Test Content</p>");

        assertFalse(result);
        verifyNoInteractions(mailSender);
    }
}
