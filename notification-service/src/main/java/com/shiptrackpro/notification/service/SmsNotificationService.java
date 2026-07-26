package com.shiptrackpro.notification.service;

public interface SmsNotificationService {
    boolean sendSms(String phone, String message);
}
