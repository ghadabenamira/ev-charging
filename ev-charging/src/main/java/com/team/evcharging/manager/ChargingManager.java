
package com.team.evcharging.manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.team.evcharging.strategy.ChargingRequest;
import com.team.evcharging.strategy.ChargingStrategy;

// Coordinates the waiting queue and the selected charging strategy.
public class ChargingManager {

    // Stores requests waiting to be processed.
    private final List<ChargingRequest> waitingQueue = new ArrayList<>();

    // Determines how the next request is selected.
    private ChargingStrategy strategy;

    // Creates the manager with an initial charging strategy.
    public ChargingManager(ChargingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException(
                "Charging strategy must not be null"
            );
        }

        this.strategy = strategy;
    }

    // Adds a new request to the waiting queue.
    public void addRequest(ChargingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                "Charging request must not be null"
            );
        }

        waitingQueue.add(request);
    }

    // Changes the strategy used to select the next request.
    public void setStrategy(ChargingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException(
                "Charging strategy must not be null"
            );
        }

        this.strategy = strategy;
    }

    // Returns the currently selected strategy.
    public ChargingStrategy getStrategy() {
        return strategy;
    }

    // Selects and removes the next request from the waiting queue.
    public ChargingRequest processNextRequest() {

        // Ask the current strategy to select a request.
        ChargingRequest selected = strategy.selectNext(
            waitingQueue,
            LocalDateTime.now()
        );

        // Remove the selected request from the queue.
        if (selected != null) {
            waitingQueue.remove(selected);
        }

        return selected;
    }

    // Returns a read-only view of the waiting queue.
    public List<ChargingRequest> getWaitingQueue() {
        return Collections.unmodifiableList(waitingQueue);
    }

    // Returns the number of requests waiting.
    public int getWaitingCount() {
        return waitingQueue.size();
    }
}


