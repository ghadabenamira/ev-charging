package com.team.evcharging.vehicle;

/** A full battery electric vehicle with its own maximum charging power. */
public class ElectricVehicle extends Vehicle {
    private final double maxChargingPowerKw;

    public ElectricVehicle(String id, String licensePlate, String model,
                           double batteryCapacityKwh, double currentChargeKwh,
                           double maxChargingPowerKw) {
        super(id, licensePlate, model, batteryCapacityKwh, currentChargeKwh);
        if (!(maxChargingPowerKw > 0)) {
            throw new IllegalArgumentException("Max charging power must be > 0");
        }
        this.maxChargingPowerKw = maxChargingPowerKw;
    }

    @Override
    public double getMaxChargingPowerKw() {
        return maxChargingPowerKw;
    }
}
