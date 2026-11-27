package com.team.evcharging.infrastructure;

import com.team.evcharging.session.ChargingSession;
import com.team.evcharging.session.PortInfo;

import java.time.LocalDateTime;

/**
 * A single physical charging point. It only tracks its own state and the session using it;
 * battery calculations belong to {@link ChargingSession} and future-booking rules belong to
 * the reservation package.
 */
public class ChargingPort implements PortInfo {

    private final String id;
    private final double powerKw;
    private PortStatus status;
    private ChargingSession currentSession;
    private long busyMinutes;

    /**
     * Package-private on purpose: ports are created only by {@link ChargingStation#addPort(String, double)},
     * so a port can never exist outside a station (composition).
     *
     * @param id      non-blank identifier (unique within its station)
     * @param powerKw charging power in kW, must be positive
     */
    ChargingPort(String id, double powerKw) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Port id must not be null or blank");
        }
        if (Double.isNaN(powerKw) || Double.isInfinite(powerKw) || powerKw <= 0) {
            throw new IllegalArgumentException("Charging power must be a positive number");
        }
        this.id = id;
        this.powerKw = powerKw;
        this.status = PortStatus.AVAILABLE;
        this.busyMinutes = 0;
    }

    public String getId() { return id; }
    @Override
    public double getPowerKw() { return powerKw; }
    public PortStatus getStatus() { return status; }
    public ChargingSession getCurrentSession() { return currentSession; }
    public long getBusyMinutes() { return busyMinutes; }

    /** True if the port can be used right now. */
    public boolean isAvailable() {
        return status == PortStatus.AVAILABLE;
    }

    /**
     * Physical availability for the interval [start, end).
     * <ul>
     *   <li>out of service: never available</li>
     *   <li>occupied: available only if the current session is expected to end by {@code start}</li>
     *   <li>available / reserved: available (other reservations are checked by ReservationService)</li>
     * </ul>
     */
    public boolean isAvailableDuring(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end must not be null");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End must be strictly after start");
        }
        switch (status) {
            case OUT_OF_SERVICE:
                return false;
            case OCCUPIED:
                LocalDateTime expectedEnd = estimatedEndOfCurrentSession();
                return expectedEnd != null && !expectedEnd.isAfter(start);
            default:
                return true;
        }
    }

    /** Starts a session on this port. Allowed from AVAILABLE or RESERVED (the reservation holder arrives). */
    public void occupy(ChargingSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Session must not be null");
        }
        if (status == PortStatus.OCCUPIED) {
            throw new IllegalStateException("Port " + id + " is already occupied");
        }
        if (status == PortStatus.OUT_OF_SERVICE) {
            throw new IllegalStateException("Port " + id + " is out of service");
        }
        this.currentSession = session;
        this.status = PortStatus.OCCUPIED;
    }

    /** Ends the current session, adds its duration to busyMinutes and frees the port. */
    public void release() {
        if (status != PortStatus.OCCUPIED) {
            throw new IllegalStateException("Port " + id + " has no active session to release");
        }
        busyMinutes += Math.max(0, currentSession.getDuration().toMinutes());
        currentSession = null;
        status = PortStatus.AVAILABLE;
    }

    /**
     * When the current session is expected to finish: its real end time if it is already over,
     * otherwise start + time charged so far + time still needed for the remaining energy at the
     * effective power (the lower of this port's power and the vehicle's maximum).
     * Returns null if there is no session or the end cannot be estimated.
     */
    LocalDateTime estimatedEndOfCurrentSession() {
        if (currentSession == null) {
            return null;
        }
        if (currentSession.getEndTime() != null) {
            return currentSession.getEndTime();
        }
        double power = Math.min(powerKw, currentSession.getVehicle().getMaxChargingPowerKw());
        if (power <= 0) {
            return null;
        }
        double remainingKwh = Math.max(0,
                currentSession.getTargetChargeKwh() - currentSession.getVehicle().getCurrentChargeKwh());
        long remainingSeconds = Math.round(remainingKwh / power * 3600);
        return currentSession.getStartTime()
                .plus(currentSession.getDuration())
                .plusSeconds(remainingSeconds);
    }

    /** Takes the port out of service. A port in use must be released first. */
    public void setOutOfService() {
        if (status == PortStatus.OCCUPIED) {
            throw new IllegalStateException("Release port " + id + " before taking it out of service");
        }
        status = PortStatus.OUT_OF_SERVICE;
    }

    /** Explicitly brings an out-of-service port back (the only way out of OUT_OF_SERVICE). */
    public void restoreService() {
        if (status != PortStatus.OUT_OF_SERVICE) {
            throw new IllegalStateException("Port " + id + " is not out of service");
        }
        status = PortStatus.AVAILABLE;
    }

    /** Marks a free port as held for an arriving reservation (used by ChargingManager). */
    public void markReserved() {
        if (status != PortStatus.AVAILABLE) {
            throw new IllegalStateException("Only an available port can be marked reserved");
        }
        status = PortStatus.RESERVED;
    }

    /** Clears the RESERVED mark (e.g. reservation cancelled or expired before arrival). */
    public void clearReserved() {
        if (status != PortStatus.RESERVED) {
            throw new IllegalStateException("Port " + id + " is not reserved");
        }
        status = PortStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return "ChargingPort{" + id + ", " + powerKw + " kW, " + status + "}";
    }
}
