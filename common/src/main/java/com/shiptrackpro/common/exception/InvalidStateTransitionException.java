package com.shiptrackpro.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a shipment status transition violates the state machine rules.
 * Maps to HTTP 422 Unprocessable Entity.
 *
 * Usage: throw new InvalidStateTransitionException("CREATED", "DELIVERED");
 * Result: "Invalid status transition from CREATED to DELIVERED"
 */
public class InvalidStateTransitionException extends ShipTrackException {

    public InvalidStateTransitionException(String fromStatus, String toStatus) {
        super(
                String.format("Invalid status transition from %s to %s", fromStatus, toStatus),
                "INVALID_STATE_TRANSITION",
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }
}
