package com.shiptrackpro.notification.service;

public interface EmailNotificationService {
    boolean sendEmail(String to, String subject, String body);
}
