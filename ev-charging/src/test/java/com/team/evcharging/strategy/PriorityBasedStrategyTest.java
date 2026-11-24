package com.team.evcharging.strategy;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

// Tests the Priority-Based charging strategy.
class PriorityBasedStrategyTest {

    @Test
    void shouldSelectHighestPriorityRequest() {

        // Create requests with different priority levels.
        ChargingRequest standard =
                new ChargingRequest(new Object(), 80, 1, "STANDARD");

        ChargingRequest premium =
                new ChargingRequest(new Object(), 80, 2, "PREMIUM");

        ChargingRequest emergency =
                new ChargingRequest(new Object(), 80, 3, "EMERGENCY");

        // Put them in a list where the highest priority is last.
        List<ChargingRequest> waiting =
                Arrays.asList(standard, premium, emergency);

        // Create the priority strategy.
        PriorityBasedStrategy strategy = new PriorityBasedStrategy();

        // Select the next request.
        ChargingRequest selected =
                strategy.selectNext(waiting, LocalDateTime.now());

        // Emergency should be selected first.
        assertSame(emergency, selected);
    }

    @Test
    void shouldUseArrivalTimeWhenPrioritiesAreEqual() {

        // Both requests have the same priority.
        ChargingRequest later =
                new ChargingRequest(new Object(), 80, 10, "PREMIUM");

        ChargingRequest earlier =
                new ChargingRequest(new Object(), 80, 5, "PREMIUM");

        // Put the later request first in the list.
        List<ChargingRequest> waiting =
                Arrays.asList(later, earlier);

        // Create the priority strategy.
        PriorityBasedStrategy strategy = new PriorityBasedStrategy();

        // Select the next request.
        ChargingRequest selected =
                strategy.selectNext(waiting, LocalDateTime.now());

        // The earlier arrival should win the tie.
        assertSame(earlier, selected);
    }

    @Test
    void shouldReturnNullWhenQueueIsEmpty() {

        // Create the priority strategy.
        PriorityBasedStrategy strategy = new PriorityBasedStrategy();

        // An empty queue should return null.
        assertNull(strategy.selectNext(
                List.of(), LocalDateTime.now()
        ));
    }
}