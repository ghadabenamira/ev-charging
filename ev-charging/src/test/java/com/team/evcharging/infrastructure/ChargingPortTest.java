package com.team.evcharging.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.team.evcharging.session.ChargingSession;
import com.team.evcharging.vehicle.ElectricVehicle;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ChargingPortTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 12, 1, 9, 0);

    private ChargingPort port;
    private ChargingSession session;

    /** 60 kWh battery, 50 kW max: on a 22 kW port the port's power is the limit. */
    private static ElectricVehicle ev(String id, double chargeKwh) {
        return new ElectricVehicle(id, "PLATE-" + id, "Test EV", 60, chargeKwh, 50);
    }

    @BeforeEach
    void setUp() {
        port = new ChargingPort("P1", 22.0);
        session = new ChargingSession(ev("V1", 10), port, T0, 60);
    }

    @Test
    void portStartsAvailable() {
        assertTrue(port.isAvailable());
        assertEquals(PortStatus.AVAILABLE, port.getStatus());
        assertNull(port.getCurrentSession());
        assertEquals(0, port.getBusyMinutes());
    }

    @Test
    void invalidConstructorArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ChargingPort(" ", 22));
        assertThrows(IllegalArgumentException.class, () -> new ChargingPort(null, 22));
        assertThrows(IllegalArgumentException.class, () -> new ChargingPort("P", 0));
        assertThrows(IllegalArgumentException.class, () -> new ChargingPort("P", -5));
    }

    @Test
    void portCanBeOccupied() {
        port.occupy(session);
        assertFalse(port.isAvailable());
        assertEquals(PortStatus.OCCUPIED, port.getStatus());
        assertSame(session, port.getCurrentSession());
    }

    @Test
    void occupyingOccupiedPortIsRejected() {
        port.occupy(session);
        ChargingSession other = new ChargingSession(ev("V2", 10), port, T0, 60);
        assertThrows(IllegalStateException.class, () -> port.occupy(other));
        assertSame(session, port.getCurrentSession());
    }

    @Test
    void occupyingWithNullSessionIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> port.occupy(null));
        assertTrue(port.isAvailable());
    }

    @Test
    void outOfServicePortCannotBeOccupied() {
        port.setOutOfService();
        assertThrows(IllegalStateException.class, () -> port.occupy(session));
        assertEquals(PortStatus.OUT_OF_SERVICE, port.getStatus());
    }

    @Test
    void releaseClearsSessionAndFreesPort() {
        port.occupy(session);
        session.tick(40);
        port.release();
        assertNull(port.getCurrentSession());
        assertEquals(PortStatus.AVAILABLE, port.getStatus());
    }

    @Test
    void busyMinutesAccumulateAcrossSessions() {
        port.occupy(session);
        session.tick(40);
        port.release();

        ChargingSession second = new ChargingSession(ev("V2", 10), port, T0.plusHours(1), 60);
        port.occupy(second);
        second.tick(25);
        port.release();

        assertEquals(65, port.getBusyMinutes());
    }

    @Test
    void releasingFreePortIsRejected() {
        assertThrows(IllegalStateException.class, () -> port.release());
    }

    @Test
    void outOfServicePortDoesNotBecomeAvailableAccidentally() {
        port.setOutOfService();
        assertThrows(IllegalStateException.class, () -> port.release());
        assertThrows(IllegalStateException.class, () -> port.markReserved());
        assertFalse(port.isAvailable());
        port.restoreService();
        assertTrue(port.isAvailable());
    }

    @Test
    void occupiedPortCannotBeTakenOutOfService() {
        port.occupy(session);
        assertThrows(IllegalStateException.class, () -> port.setOutOfService());
    }

    @Test
    void reservedPortCanBeOccupiedByArrivingVehicle() {
        port.markReserved();
        assertEquals(PortStatus.RESERVED, port.getStatus());
        assertFalse(port.isAvailable());
        port.occupy(session);
        assertEquals(PortStatus.OCCUPIED, port.getStatus());
    }

    @Test
    void estimatedEndUsesRemainingEnergyAndEffectivePower() {
        // 11 kWh missing at 22 kW = 30 minutes
        ChargingSession s = new ChargingSession(ev("V3", 49), port, T0, 60);
        port.occupy(s);
        assertEquals(T0.plusMinutes(30), port.estimatedEndOfCurrentSession());

        s.tick(10);   // 10 minutes charged, 20 minutes still needed
        assertEquals(T0.plusMinutes(30), port.estimatedEndOfCurrentSession());

        s.tick(60);   // completes: the real end time is used
        assertEquals(s.getEndTime(), port.estimatedEndOfCurrentSession());
    }

    @Test
    void availabilityDuringIntervalDependsOnStatus() {
        LocalDateTime start = LocalDateTime.of(2026, 12, 1, 10, 0);
        LocalDateTime end = start.plusHours(1);
        assertTrue(port.isAvailableDuring(start, end));

        port.setOutOfService();
        assertFalse(port.isAvailableDuring(start, end));
        port.restoreService();

        // A session that will still be charging at "start": not available
        ChargingSession running = new ChargingSession(ev("V3", 49), port, start, 60);
        port.occupy(running);
        assertFalse(port.isAvailableDuring(start, end));
        port.release();

        // A session expected to finish exactly at "start": available
        ChargingSession endsAtStart = new ChargingSession(ev("V4", 49), port, start.minusMinutes(30), 60);
        port.occupy(endsAtStart);
        assertTrue(port.isAvailableDuring(start, end));

        assertThrows(IllegalArgumentException.class, () -> port.isAvailableDuring(end, start));
    }
}
