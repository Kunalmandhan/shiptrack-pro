package com.shiptrackpro.notification.exception;

import com.shiptrackpro.common.exception.GlobalExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler for Notification Service.
 * Inherits generic exception handling from GlobalExceptionHandler.
 */
@RestControllerAdvice
public class NotificationExceptionHandler extends GlobalExceptionHandler {
}
