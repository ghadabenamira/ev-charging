package com.team.evcharging.manager;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.team.evcharging.strategy.ChargingRequest;
import com.team.evcharging.strategy.FirstComeFirstServedStrategy;
import com.team.evcharging.strategy.PriorityBasedStrategy;

// Tests the basic queue and strategy management operations.
class ChargingManagerTest {

    @Test
    void shouldAddRequestToWaitingQueue() {

        // Create a manager using the FCFS strategy.
        ChargingManager manager =
                new ChargingManager(new FirstComeFirstServedStrategy());

        // Create a sample charging request.
        ChargingRequest request =
                new ChargingRequest(new Object(), 80, 10, "STANDARD");

        // Add the request to the manager.
        manager.addRequest(request);

        // Check that the request was added.
        assertEquals(1, manager.getWaitingCount());
        assertTrue(manager.getWaitingQueue().contains(request));
    }

    @Test
    void shouldProcessNextRequest() {

        // Create a manager using the FCFS strategy.
        ChargingManager manager =
                new ChargingManager(new FirstComeFirstServedStrategy());

        // Create two requests with different arrival times.
        ChargingRequest later =
                new ChargingRequest(new Object(), 80, 10, "STANDARD");

        ChargingRequest earlier =
                new ChargingRequest(new Object(), 80, 5, "STANDARD");

        // Add the later request first.
        manager.addRequest(later);
        manager.addRequest(earlier);

        // Process the next request.
        ChargingRequest selected = manager.processNextRequest();

        // FCFS should select the earlier request.
        assertSame(earlier, selected);

        // The selected request should be removed from the queue.
        assertEquals(1, manager.getWaitingCount());
        assertTrue(manager.getWaitingQueue().contains(later));
    }

    @Test
    void shouldAllowChangingStrategy() {

        // Create a manager with FCFS.
        ChargingManager manager =
                new ChargingManager(new FirstComeFirstServedStrategy());

        // Change its strategy to Priority-Based.
        PriorityBasedStrategy priorityStrategy =
                new PriorityBasedStrategy();

        manager.setStrategy(priorityStrategy);

        // Verify that the new strategy is active.
        assertSame(priorityStrategy, manager.getStrategy());
    }

    @Test
    void shouldRejectNullStrategy() {

        // A manager cannot operate without a strategy.
        assertThrows(IllegalArgumentException.class, () ->
                new ChargingManager(null)
        );
    }
}