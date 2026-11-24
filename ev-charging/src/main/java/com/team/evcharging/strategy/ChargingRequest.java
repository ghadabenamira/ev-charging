
package com.team.evcharging.strategy;

// Represents one vehicle's request to charge.
// TEMPORARY: Object and String will be replaced with the real
// Vehicle and PriorityLevel types when teammates provide them.
public class ChargingRequest {

    private final Object vehicle;
    private final double targetCharge;
    private final long arrivalTime;
    private final String priorityLevel;

    // Creates a new charging request.
    public ChargingRequest(Object vehicle, double targetCharge,
                           long arrivalTime, String priorityLevel) {

        // Check that the required objects are provided.
        if (vehicle == null || priorityLevel == null) {
            throw new IllegalArgumentException(
                "Vehicle and priority level must not be null"
            );
        }

        // The target charge must be a valid percentage.
        if (!Double.isFinite(targetCharge)
                || targetCharge <= 0 || targetCharge > 100) {
            throw new IllegalArgumentException(
                "Target charge must be between 0 and 100"
            );
        }

        // Arrival time cannot be negative.
        if (arrivalTime < 0) {
            throw new IllegalArgumentException(
                "Arrival time cannot be negative"
            );
        }

        this.vehicle = vehicle;
        this.targetCharge = targetCharge;
        this.arrivalTime = arrivalTime;
        this.priorityLevel = priorityLevel;
    }

    // Returns the vehicle associated with this request.
    public Object getVehicle() {
        return vehicle;
    }

    // Returns the requested charge percentage.
    public double getTargetCharge() {
        return targetCharge;
    }

    // Returns when the request arrived.
    public long getArrivalTime() {
        return arrivalTime;
    }

    // Returns the request's priority.
    public String getPriorityLevel() {
        return priorityLevel;
    }
}
