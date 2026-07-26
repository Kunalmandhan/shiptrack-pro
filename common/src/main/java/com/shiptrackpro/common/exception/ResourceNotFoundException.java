package com.shiptrackpro.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested entity does not exist in the database.
 * Maps to HTTP 404 Not Found.
 *
 * Usage: throw new ResourceNotFoundException("User", "id", userId);
 * Result: "User not found with id: 550e8400-..."
 */
public class ResourceNotFoundException extends ShipTrackException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue),
                "RESOURCE_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }
}
