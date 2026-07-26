package com.shiptrackpro.notification.service.impl;

import com.shiptrackpro.notification.service.SmsNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsNotificationServiceImpl implements SmsNotificationService {

    @Override
    public boolean sendSms(String phone, String message) {
        if (phone == null || phone.isBlank()) {
            log.warn("Cannot send SMS: phone number is empty");
            return false;
        }

        // Twilio / SMS provider integration stub
        log.info("[SMS Stub] Sent SMS to {}: {}", phone, message);
        return true;
    }
}
