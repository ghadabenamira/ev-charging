package com.team.evcharging.vehicle;

/** Base class: holds the battery and the charge maths shared by all vehicles. */
public abstract class Vehicle {
    private final String id;
    private final String licensePlate;
    private final String model;
    private final double batteryCapacityKwh;
    private double currentChargeKwh;

    protected Vehicle(String id, String licensePlate, String model,
                      double batteryCapacityKwh, double currentChargeKwh) {
        if (id == null || id.isBlank() || licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException("id and licensePlate are required");
        }
        if (!(batteryCapacityKwh > 0) || Double.isInfinite(batteryCapacityKwh)) {
            throw new IllegalArgumentException("Battery capacity must be > 0");
        }
        if (Double.isNaN(currentChargeKwh) || currentChargeKwh < 0
                || currentChargeKwh > batteryCapacityKwh) {
            throw new IllegalArgumentException("Charge must be between 0 and capacity");
        }
        this.id = id;
        this.licensePlate = licensePlate;
        this.model = model;
        this.batteryCapacityKwh = batteryCapacityKwh;
        this.currentChargeKwh = currentChargeKwh;
    }

    /** Fastest power (kW) this vehicle can accept. Each vehicle type defines it. */
    public abstract double getMaxChargingPowerKw();

    /** Adds energy, never above capacity. Returns the energy actually added. */
    public double addCharge(double kWh) {
        if (Double.isNaN(kWh) || kWh < 0) {
            throw new IllegalArgumentException("Energy to add must be >= 0");
        }
        double added = Math.min(kWh, getRemainingCapacity());
        currentChargeKwh += added;
        return added;
    }

    public double getStateOfCharge() {
        return currentChargeKwh / batteryCapacityKwh * 100.0;
    }

    public double getRemainingCapacity() {
        return batteryCapacityKwh - currentChargeKwh;
    }

    public boolean isFull() {
        return getRemainingCapacity() <= 1e-9;
    }

    public String getId() { return id; }
    public String getLicensePlate() { return licensePlate; }
    public String getModel() { return model; }
    public double getBatteryCapacityKwh() { return batteryCapacityKwh; }
    public double getCurrentChargeKwh() { return currentChargeKwh; }

    @Override
    public String toString() {
        return String.format("%s [%s] %.1f%%", model, licensePlate, getStateOfCharge());
    }
}