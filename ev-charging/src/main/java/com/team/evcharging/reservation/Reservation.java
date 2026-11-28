package com.team.evcharging.reservation;

import com.team.evcharging.infrastructure.ChargingPort;
import com.team.evcharging.infrastructure.ChargingStation;
import com.team.evcharging.vehicle.Vehicle;

import java.time.LocalDateTime;

/** A booking of one port at one station for the half-open interval [startTime, endTime). */
public class Reservation {

    private final String id;
    private final Vehicle vehicle;
    private final ChargingStation station;
    private final ChargingPort port;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private ReservationStatus status;

    public Reservation(String id, Vehicle vehicle, ChargingStation station, ChargingPort port,
                       LocalDateTime startTime, LocalDateTime endTime) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Reservation id must not be null or blank");
        }
        if (vehicle == null || station == null || port == null) {
            throw new IllegalArgumentException("Vehicle, station and port must not be null");
        }
        if (!station.getPorts().contains(port)) {
            throw new IllegalArgumentException("Port " + port.getId() + " does not belong to station " + station.getId());
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and end time must not be null");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be strictly after start time");
        }
        this.id = id;
        this.vehicle = vehicle;
        this.station = station;
        this.port = port;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = ReservationStatus.ACTIVE;
    }

    public String getId() { return id; }
    public Vehicle getVehicle() { return vehicle; }
    public ChargingStation getStation() { return station; }
    public ChargingPort getPort() { return port; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public ReservationStatus getStatus() { return status; }

        /**
     * Read-only check used before a reservation exists: does this ACTIVE reservation
     * block the given port for [from, to)? Back-to-back slots do not conflict.
     */
    public boolean blocks(ChargingPort otherPort, LocalDateTime from, LocalDateTime to) {
        if (status != ReservationStatus.ACTIVE || port != otherPort) {
            return false;
        }
        return startTime.isBefore(to) && from.isBefore(endTime);
    }

/**
     * True if both reservations are ACTIVE, use the same port and their time intervals overlap.
     * Adjacent intervals (10:00-11:00 and 11:00-12:00) do not overlap.
     */
    public boolean overlaps(Reservation other) {
        if (other == null) {
            throw new IllegalArgumentException("Other reservation must not be null");
        }
        if (status != ReservationStatus.ACTIVE || other.status != ReservationStatus.ACTIVE) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        return startTime.isBefore(other.endTime) && other.startTime.isBefore(endTime);
    }

    /** True if the reservation is ACTIVE and {@code time} is within [start, end). */
    public boolean isActiveAt(LocalDateTime time) {
        if (time == null) {
            throw new IllegalArgumentException("Time must not be null");
        }
        return status == ReservationStatus.ACTIVE && !time.isBefore(startTime) && time.isBefore(endTime);
    }

    /** ACTIVE -> CANCELLED. */
    public void cancel() {
        transitionTo(ReservationStatus.CANCELLED);
    }

    /** ACTIVE -> COMPLETED (the vehicle used the slot). */
    public void complete() {
        transitionTo(ReservationStatus.COMPLETED);
    }

    /** ACTIVE -> EXPIRED (the vehicle never showed up). */
    public void expire() {
        transitionTo(ReservationStatus.EXPIRED);
    }

    private void transitionTo(ReservationStatus target) {
        if (status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Reservation " + id + " is " + status + " and cannot become " + target);
        }
        status = target;
    }

    @Override
    public String toString() {
        return "Reservation{" + id + ", vehicle=" + vehicle.getId() + ", station=" + station.getId()
                + ", port=" + port.getId() + ", " + startTime + " -> " + endTime + ", " + status + "}";
    }
}
