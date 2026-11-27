package com.team.evcharging.reservation;

/** Lifecycle of a reservation. Only ACTIVE can change; the other states are final. */
public enum ReservationStatus {
    ACTIVE,
    CANCELLED,
    COMPLETED,
    EXPIRED
}
