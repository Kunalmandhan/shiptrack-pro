package com.shiptrackpro.user.exception;

import com.shiptrackpro.common.exception.GlobalExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * User Service exception handler.
 * Extends GlobalExceptionHandler to inherit all standard error handling.
 *
 * Add User-service-specific exception handlers here as needed.
 * Current coverage from GlobalExceptionHandler:
 * - ShipTrackException (all business exceptions)
 * - MethodArgumentNotValidException (validation errors)
 * - AccessDeniedException (403)
 * - AuthenticationException (401)
 * - IllegalArgumentException (400)
 * - Exception (catch-all, 500)
 */
@RestControllerAdvice
public class UserExceptionHandler extends GlobalExceptionHandler {
    // Inherits all handlers from GlobalExceptionHandler
    // Add User-service-specific handlers below if needed
}
