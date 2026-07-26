package com.shiptrackpro.shipment.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Shipment status enum with a built-in state machine.
 *
 * Valid transitions are defined in VALID_TRANSITIONS map.
 * Any transition not in this map throws InvalidStateTransitionException.
 *
 * Terminal states (no outgoing transitions): DELIVERED, CANCELLED, RETURNED.
 */
public enum ShipmentStatus {

    CREATED,
    PROCESSING,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    DELAYED,
    FAILED_DELIVERY,
    CANCELLED,
    RETURNED;

    private static final Map<ShipmentStatus, Set<ShipmentStatus>> VALID_TRANSITIONS = Map.of(
            CREATED, EnumSet.of(PROCESSING, CANCELLED),
            PROCESSING, EnumSet.of(PICKED_UP, CANCELLED),
            PICKED_UP, EnumSet.of(IN_TRANSIT),
            IN_TRANSIT, EnumSet.of(OUT_FOR_DELIVERY, DELAYED),
            DELAYED, EnumSet.of(IN_TRANSIT),
            OUT_FOR_DELIVERY, EnumSet.of(DELIVERED, FAILED_DELIVERY),
            FAILED_DELIVERY, EnumSet.of(OUT_FOR_DELIVERY, RETURNED)
    );

    /**
     * Check if transitioning from this status to the target is allowed.
     *
     * @param target the desired next status
     * @return true if the transition is valid
     */
    public boolean canTransitionTo(ShipmentStatus target) {
        Set<ShipmentStatus> allowed = VALID_TRANSITIONS.get(this);
        return allowed != null && allowed.contains(target);
    }

    /**
     * Returns true if this is a terminal state (no further transitions).
     */
    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED || this == RETURNED;
    }
}
