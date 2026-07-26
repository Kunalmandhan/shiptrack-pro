package com.shiptrackpro.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for all ShipTrack Pro business exceptions.
 * Carries an errorCode and HTTP status for consistent error responses.
 *
 * All custom exceptions extend this class, so the GlobalExceptionHandler
 * can catch ShipTrackException and extract errorCode + status automatically.
 */
@Getter
public class ShipTrackException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public ShipTrackException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public ShipTrackException(String message, String errorCode, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }
}
