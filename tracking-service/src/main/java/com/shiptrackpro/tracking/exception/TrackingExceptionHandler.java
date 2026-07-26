package com.shiptrackpro.tracking.exception;

import com.shiptrackpro.common.exception.GlobalExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler for Tracking Service.
 * Inherits generic exception handling from GlobalExceptionHandler.
 */
@RestControllerAdvice
public class TrackingExceptionHandler extends GlobalExceptionHandler {
}
