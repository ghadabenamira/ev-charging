package com.team.evcharging.reservation;

import com.team.evcharging.infrastructure.ChargingPort;
import com.team.evcharging.infrastructure.ChargingStation;
import com.team.evcharging.vehicle.Vehicle;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Creates, cancels and looks up reservations, and prevents double-booking.
 * Cancelling only changes the reservation: it never touches the port, so a session
 * currently using that port is never disturbed.
 */
public class ReservationService {

    private final Map<String, Reservation> reservations = new LinkedHashMap<>();
    private int nextNumber = 1;

    /**
     * Reserves the first port of the station that is physically usable and free of
     * overlapping ACTIVE reservations during [start, end).
     *
     * @throws ReservationConflictException if every suitable port is already booked
     * @throws IllegalArgumentException     for null arguments or an invalid interval
     */
    public Reservation createReservation(Vehicle vehicle, ChargingStation station,
                                         LocalDateTime start, LocalDateTime end)
            throws ReservationConflictException {
        if (vehicle == null) throw new IllegalArgumentException("Vehicle must not be null");
        if (station == null) throw new IllegalArgumentException("Station must not be null");
        if (start == null || end == null) throw new IllegalArgumentException("Start and end must not be null");
        if (!end.isAfter(start)) throw new IllegalArgumentException("End must be strictly after start");

        String id = String.format("RES-%04d", nextNumber);
        for (ChargingPort port : station.getPorts()) {
            if (!port.isAvailableDuring(start, end)) {
                continue; // out of service, or busy with a session that outlasts the slot start
            }
            Reservation candidate = new Reservation(id, vehicle, station, port, start, end);
            if (!conflictsWithExisting(candidate)) {
                reservations.put(id, candidate);
                nextNumber++;
                return candidate;
            }
        }
        throw new ReservationConflictException("No port available at station '" + station.getName()
                + "' between " + start + " and " + end);
    }

    /**
     * Read-only: could a reservation for [start, end) be made at this station right now?
     * Creates nothing and changes nothing. Used by the assistant to suggest alternatives.
     */
    public boolean isSlotAvailable(ChargingStation station, LocalDateTime start, LocalDateTime end) {
        if (station == null) throw new IllegalArgumentException("Station must not be null");
        if (start == null || end == null) throw new IllegalArgumentException("Start and end must not be null");
        if (!end.isAfter(start)) throw new IllegalArgumentException("End must be strictly after start");
        for (ChargingPort port : station.getPorts()) {
            if (!port.isAvailableDuring(start, end)) {
                continue;
            }
            boolean taken = false;
            for (Reservation existing : reservations.values()) {
                if (existing.blocks(port, start, end)) {
                    taken = true;
                    break;
                }
            }
            if (!taken) {
                return true;
            }
        }
        return false;
    }

    /** Cancels an ACTIVE reservation, which frees its slot for new bookings. */
    public void cancelReservation(String id) {
        Reservation reservation = requireReservation(id);
        reservation.cancel(); // throws IllegalStateException if it is not ACTIVE
    }

    /** @return the reservation, or empty if the id is unknown */
    public Optional<Reservation> getReservation(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Reservation id must not be null or blank");
        }
        return Optional.ofNullable(reservations.get(id));
    }

    /** All reservations (any status) of a vehicle, in creation order. Returns a copy. */
    public List<Reservation> getReservationsByVehicle(Vehicle vehicle) {
        if (vehicle == null) throw new IllegalArgumentException("Vehicle must not be null");
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            if (r.getVehicle().getId().equals(vehicle.getId())) {
                result.add(r);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /** All reservations (any status) at a station, in creation order. Returns a copy. */
    public List<Reservation> getReservationsByStation(ChargingStation station) {
        if (station == null) throw new IllegalArgumentException("Station must not be null");
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            if (r.getStation() == station) {
                result.add(r);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private boolean conflictsWithExisting(Reservation candidate) {
        for (Reservation existing : reservations.values()) {
            if (existing.overlaps(candidate)) {
                return true;
            }
        }
        return false;
    }

    private Reservation requireReservation(String id) {
        return getReservation(id).orElseThrow(
                () -> new IllegalArgumentException("Unknown reservation id: " + id));
    }
}
