package com.shiptrackpro.auth.exception;

import com.shiptrackpro.common.dto.ApiResponse;
import com.shiptrackpro.common.exception.GlobalExceptionHandler;
import com.shiptrackpro.common.exception.ShipTrackException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * Auth-service specific exception handler that extends the global handler.
 * Adds handling for inter-service communication errors (User Service down,
 * User Service returning 4xx, etc.) without leaking internal details.
 */
@Slf4j
@RestControllerAdvice
public class AuthExceptionHandler extends GlobalExceptionHandler {

    /**
     * User Service returned a 4xx error (email already exists, user not found, etc.).
     * Unwrap the status and forward a clean message to the client.
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpClientError(
            HttpClientErrorException ex, HttpServletRequest request) {
        log.error("User Service client error: {} | Path: {}", ex.getStatusCode(), request.getRequestURI());

        String message = switch (ex.getStatusCode().value()) {
            case 404 -> "User not found";
            case 409 -> "A user with this email already exists";
            default -> "Service communication error";
        };

        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiResponse.error(message, "USER_SERVICE_ERROR", request.getRequestURI()));
    }

    /**
     * User Service is unreachable (connection refused, timeout, etc.).
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleServiceUnavailable(
            ResourceAccessException ex, HttpServletRequest request) {
        log.error("User Service unavailable: {} | Path: {}", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("An internal service is temporarily unavailable. Please try again later.",
                        "SERVICE_UNAVAILABLE", request.getRequestURI()));
    }
}
