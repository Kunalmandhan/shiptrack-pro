package com.shiptrackpro.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when attempting to create a resource that already exists.
 * Maps to HTTP 409 Conflict.
 *
 * Usage: throw new DuplicateResourceException("User", "email", email);
 * Result: "User already exists with email: john@example.com"
 */
public class DuplicateResourceException extends ShipTrackException {

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s already exists with %s: %s", resourceName, fieldName, fieldValue),
                "DUPLICATE_RESOURCE",
                HttpStatus.CONFLICT
        );
    }
}
