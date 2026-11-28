package com.team.evcharging.reservation;

/** Thrown when no port of a station can accommodate the requested time slot. */
public class ReservationConflictException extends Exception {

    public ReservationConflictException(String message) {
        super(message);
    }
}
