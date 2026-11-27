package com.team.evcharging.session;

import com.team.evcharging.vehicle.Vehicle;
import java.time.Duration;
import java.time.LocalDateTime;

/** One charging session: delivers energy to a vehicle through a port. */
public class ChargingSession {
    private static final double EPS = 1e-9;

    private final Vehicle vehicle;
    private final PortInfo port;
    private final LocalDateTime startTime;
    private final double targetChargeKwh;
    private double energyDelivered;
    private double elapsedMinutes;
    private LocalDateTime endTime;
    private SessionStatus status;

    public ChargingSession(Vehicle vehicle, PortInfo port,
                           LocalDateTime startTime, double targetChargeKwh) {
        if (vehicle == null || port == null || startTime == null) {
            throw new IllegalArgumentException("vehicle, port and startTime are required");
        }
        if (!(targetChargeKwh > 0) || targetChargeKwh > vehicle.getBatteryCapacityKwh() + EPS) {
            throw new IllegalArgumentException("Target must be > 0 and <= battery capacity");
        }
        this.vehicle = vehicle;
        this.port = port;
        this.startTime = startTime;
        this.targetChargeKwh = targetChargeKwh;
        this.status = SessionStatus.ACTIVE;
        if (remainingToTarget() <= EPS) {      // already at target (or full)
            status = SessionStatus.COMPLETED;
            endTime = startTime;
        }
    }

    /** Charges for the given minutes. Returns the energy (kWh) delivered in this step. */
    public double tick(long minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("Minutes must be > 0");
        }
        if (status != SessionStatus.ACTIVE) {
            return 0;
        }
        double power = Math.min(port.getPowerKw(), vehicle.getMaxChargingPowerKw());
        double wanted = power * (minutes / 60.0);
        double added = vehicle.addCharge(Math.min(wanted, remainingToTarget()));
        energyDelivered += added;

        if (remainingToTarget() <= EPS) {
            elapsedMinutes += power > 0 ? added / power * 60.0 : 0;
            status = SessionStatus.COMPLETED;
            endTime = startTime.plusSeconds(Math.round(elapsedMinutes * 60));
        } else {
            elapsedMinutes += minutes;
        }
        return added;
    }

    /** Stops the session early. The charge already delivered is kept. */
    public void stop() {
        if (status == SessionStatus.ACTIVE) {
            status = SessionStatus.INTERRUPTED;
            endTime = startTime.plusSeconds(Math.round(elapsedMinutes * 60));
        }
    }

    public boolean isComplete() {
        return status == SessionStatus.COMPLETED;
    }

    public Duration getDuration() {
        return Duration.ofSeconds(Math.round(elapsedMinutes * 60));
    }

    private double remainingToTarget() {
        return Math.max(0, targetChargeKwh - vehicle.getCurrentChargeKwh());
    }

    public Vehicle getVehicle() { return vehicle; }
    public PortInfo getPort() { return port; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public double getTargetChargeKwh() { return targetChargeKwh; }
    public double getEnergyDelivered() { return energyDelivered; }
    public SessionStatus getStatus() { return status; }
}