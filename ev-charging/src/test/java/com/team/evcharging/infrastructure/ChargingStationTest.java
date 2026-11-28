package com.team.evcharging.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.team.evcharging.session.ChargingSession;
import com.team.evcharging.vehicle.ElectricVehicle;
import com.team.evcharging.vehicle.Vehicle;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ChargingStationTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 12, 1, 10, 0);
    private static final LocalDateTime END = START.plusHours(1);

    private ChargingStation station;

    @BeforeEach
    void setUp() {
        station = new ChargingStation("S1", "Lac 2", "Tunis");
    }

    @Test
    void stationCanContainMultiplePorts() {
        station.addPort("P1", 22);
        station.addPort("P2", 50);
        station.addPort("P3", 7.4);
        assertEquals(3, station.getTotalPorts());
        assertEquals(3, station.getPorts().size());
    }

    @Test
    void duplicatePortIdIsRejected() {
        station.addPort("P1", 22);
        assertThrows(IllegalArgumentException.class, () -> station.addPort("P1", 50));
        assertEquals(1, station.getTotalPorts());
    }

    @Test
    void portListCannotBeModifiedFromOutside() {
        station.addPort("P1", 22);
        assertThrows(UnsupportedOperationException.class, () -> station.getPorts().clear());
        assertThrows(UnsupportedOperationException.class, () -> station.getPorts().add(new ChargingPort("X", 1)));
    }

    @Test
    void availablePortsAreReturnedCorrectly() {
        ChargingPort p1 = station.addPort("P1", 22);
        ChargingPort p2 = station.addPort("P2", 22);
        p1.occupy(new ChargingSession(new ElectricVehicle("V1", "PLATE-V1", "Test EV", 60, 10, 50), p1,
                java.time.LocalDateTime.of(2026, 12, 1, 9, 0), 60));
        assertEquals(1, station.getAvailablePorts().size());
        assertSame(p2, station.getAvailablePorts().get(0));
    }

    @Test
    void outOfServicePortsAreExcluded() {
        ChargingPort p1 = station.addPort("P1", 22);
        ChargingPort p2 = station.addPort("P2", 22);
        p1.setOutOfService();
        assertEquals(1, station.getAvailablePorts().size());
        assertSame(p2, station.findAvailablePort(START, END));
    }

    @Test
    void portIsFoundForSuitableTimeSlot() {
        ChargingPort p1 = station.addPort("P1", 22);
        assertSame(p1, station.findAvailablePort(START, END));
    }

    @Test
    void noPortReturnedWhenNoneAvailable() {
        assertNull(station.findAvailablePort(START, END)); // empty station
        station.addPort("P1", 22).setOutOfService();
        assertNull(station.findAvailablePort(START, END));
    }

    @Test
    void invalidSearchIntervalIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> station.findAvailablePort(END, START));
        assertThrows(IllegalArgumentException.class, () -> station.findAvailablePort(null, END));
    }

    @Test
    void invalidStationArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ChargingStation("", "n", "l"));
        assertThrows(IllegalArgumentException.class, () -> new ChargingStation("i", null, "l"));
        assertThrows(IllegalArgumentException.class, () -> new ChargingStation("i", "n", " "));
    }

    @Test
    void portsCanOnlyBeCreatedThroughTheirStation() throws Exception {
        java.lang.reflect.Constructor<ChargingPort> constructor =
                ChargingPort.class.getDeclaredConstructor(String.class, double.class);
        assertFalse(java.lang.reflect.Modifier.isPublic(constructor.getModifiers()));
        ChargingPort created = station.addPort("P1", 22);
        assertSame(created, station.getPorts().get(0));
    }
}
