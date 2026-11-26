package com.team.evcharging.vehicle;

/** Charging priority, ordered by importance. Higher weight = served first. */
public enum PriorityLevel {
    EMERGENCY(3),
    PREMIUM(2),
    STANDARD(1);

    private final int weight;

    PriorityLevel(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}