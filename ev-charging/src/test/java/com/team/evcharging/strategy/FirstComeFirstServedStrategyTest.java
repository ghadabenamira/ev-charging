package com.team.evcharging.strategy;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

// Tests the First Come, First Served strategy.
class FirstComeFirstServedStrategyTest {

    @Test
    void shouldSelectEarliestRequest() {

        // Create three requests with different arrival times.
        ChargingRequest requestA =
                new ChargingRequest(new Object(), 80, 10, "STANDARD");

        ChargingRequest requestB =
                new ChargingRequest(new Object(), 90, 5, "PREMIUM");

        ChargingRequest requestC =
                new ChargingRequest(new Object(), 70, 8, "EMERGENCY");

        // Put requests in an order that is not chronological.
        List<ChargingRequest> waiting =
                Arrays.asList(requestA, requestB, requestC);

        // Create the FCFS strategy.
        FirstComeFirstServedStrategy strategy =
                new FirstComeFirstServedStrategy();

        // Select the earliest request.
        ChargingRequest selected =
                strategy.selectNext(waiting, LocalDateTime.now());

        // Request B arrived first, so it should be selected.
        assertSame(requestB, selected);
    }

    @Test
    void shouldReturnNullWhenQueueIsEmpty() {

        // Create the strategy.
        FirstComeFirstServedStrategy strategy =
                new FirstComeFirstServedStrategy();

        // An empty queue should return null.
        assertNull(strategy.selectNext(
                List.of(), LocalDateTime.now()
        ));
    }
}