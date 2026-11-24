
package com.team.evcharging.strategy;

import java.time.LocalDateTime;
import java.util.List;

// Selects charging requests according to their priority.
// Higher-priority requests are processed before lower-priority ones.
public class PriorityBasedStrategy implements ChargingStrategy {

    // Finds the highest-priority request in the waiting queue.
    @Override
    public ChargingRequest selectNext(List<ChargingRequest> waiting,
                                      LocalDateTime now) {

        // If the queue is empty, there is nothing to select.
        if (waiting == null || waiting.isEmpty()) {
            return null;
        }

        // Initially, no request has been selected.
        ChargingRequest selected = null;

        // Examine every request in the waiting queue.
        for (ChargingRequest request : waiting) {

            // Ignore null entries in the queue.
            if (request == null) {
                continue;
            }

            // Select the first valid request.
            if (selected == null) {
                selected = request;
                continue;
            }

            // Compare priorities: EMERGENCY > PREMIUM > STANDARD.
            int currentPriority = getPriorityValue(request.getPriorityLevel());
            int selectedPriority = getPriorityValue(selected.getPriorityLevel());

            // A higher priority value means this request comes first.
            if (currentPriority > selectedPriority) {
                selected = request;
            }

            // If priorities are equal, choose the earlier arrival.
            else if (currentPriority == selectedPriority
                    && request.getArrivalTime() < selected.getArrivalTime()) {
                selected = request;
            }
        }

        // Return the best request found.
        return selected;
    }

    // Converts each priority level into a numerical ranking.
    // This makes priority comparisons easier.
    private int getPriorityValue(String priorityLevel) {

        if ("EMERGENCY".equalsIgnoreCase(priorityLevel)) {
            return 3;
        }

        if ("PREMIUM".equalsIgnoreCase(priorityLevel)) {
            return 2;
        }

        if ("STANDARD".equalsIgnoreCase(priorityLevel)) {
            return 1;
        }

        // Unknown priorities receive the lowest ranking.
        return 0;
    }

    // Returns the strategy's name.
    @Override
    public String getName() {
        return "Priority-Based Strategy";
    }
}
