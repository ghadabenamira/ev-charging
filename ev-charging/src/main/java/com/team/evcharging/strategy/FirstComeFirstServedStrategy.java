
package com.team.evcharging.strategy;

import java.time.LocalDateTime;
import java.util.List;

// Implements First Come, First Served (FCFS).
// The request that arrived earliest gets selected first.
public class FirstComeFirstServedStrategy implements ChargingStrategy {

    // Selects the earliest request from the waiting queue.
    @Override
    public ChargingRequest selectNext(List<ChargingRequest> waiting,
                                      LocalDateTime now) {

        // Return null if there are no requests to process.
        if (waiting == null || waiting.isEmpty()) {
            return null;
        }

        // Start by assuming the first request arrived earliest.
        ChargingRequest earliest = waiting.get(0);

        // Compare the arrival times of all remaining requests.
        for (ChargingRequest request : waiting) {

            // Replace the current selection if this request arrived earlier.
            if (request.getArrivalTime() < earliest.getArrivalTime()) {
                earliest = request;
            }
        }

        // Return the request with the earliest arrival time.
        return earliest;
    }

    // Provides a readable name for this strategy.
    @Override
    public String getName() {
        return "First Come, First Served";
    }
}
