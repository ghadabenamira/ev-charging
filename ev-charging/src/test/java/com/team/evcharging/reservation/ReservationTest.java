package com.team.evcharging.reservation;

import com.team.evcharging.infrastructure.ChargingPort;
import com.team.evcharging.infrastructure.ChargingStation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.team.evcharging.vehicle.ElectricVehicle;
import com.team.evcharging.vehicle.Vehicle;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReservationTest {

    private ChargingStation station;
    private ChargingPort port1;
    private ChargingPort port2;
    private Vehicle v1;
    private Vehicle v2;

    private static LocalDateTime at(int hour, int minute) {
        return LocalDateTime.of(2026, 12, 1, hour, minute);
    }

    private Reservation res(String id, Vehicle v, ChargingPort p, LocalDateTime s, LocalDateTime e) {
        return new Reservation(id, v, station, p, s, e);
    }

    @BeforeEach
    void setUp() {
        station = new ChargingStation("S1", "Lac 2", "Tunis");
        port1 = station.addPort("P1", 22);
        port2 = station.addPort("P2", 22);
        v1 = new ElectricVehicle("V1", "PLATE-V1", "Test EV", 60, 10, 50);
        v2 = new ElectricVehicle("V2", "PLATE-V2", "Test EV", 60, 10, 50);
    }

    @Test
    void validReservationCanBeCreated() {
        Reservation r = res("R1", v1, port1, at(10, 0), at(11, 0));
        assertEquals("R1", r.getId());
        assertSame(v1, r.getVehicle());
        assertSame(station, r.getStation());
        assertSame(port1, r.getPort());
        assertEquals(ReservationStatus.ACTIVE, r.getStatus());
    }

    @Test
    void invalidTimeIntervalIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> res("R1", v1, port1, at(11, 0), at(10, 0)));
        assertThrows(IllegalArgumentException.class, () -> res("R1", v1, port1, at(10, 0), at(10, 0)));
        assertThrows(IllegalArgumentException.class, () -> res("R1", v1, port1, null, at(10, 0)));
    }

    @Test
    void portMustBelongToStation() {
        ChargingPort foreign = new ChargingStation("S9", "Other", "Sfax").addPort("P1", 22);
        assertThrows(IllegalArgumentException.class, () -> res("R1", v1, foreign, at(10, 0), at(11, 0)));
    }

    @Test
    void overlappingReservationsAreDetected() {
        Reservation a = res("A", v1, port1, at(10, 0), at(11, 0));
        Reservation b = res("B", v2, port1, at(10, 30), at(11, 30));
        Reservation inside = res("C", v2, port1, at(10, 15), at(10, 45));
        assertTrue(a.overlaps(b));
        assertTrue(b.overlaps(a));
        assertTrue(a.overlaps(inside));
    }

    @Test
    void adjacentReservationsDoNotOverlap() {
        Reservation a = res("A", v1, port1, at(10, 0), at(11, 0));
        Reservation b = res("B", v2, port1, at(11, 0), at(12, 0));
        assertFalse(a.overlaps(b));
        assertFalse(b.overlaps(a));
    }

    @Test
    void differentPortsDoNotOverlap() {
        Reservation a = res("A", v1, port1, at(10, 0), at(11, 0));
        Reservation b = res("B", v2, port2, at(10, 0), at(11, 0));
        assertFalse(a.overlaps(b));
    }

    @Test
    void cancelledReservationDoesNotBlockOthers() {
        Reservation a = res("A", v1, port1, at(10, 0), at(11, 0));
        Reservation b = res("B", v2, port1, at(10, 0), at(11, 0));
        a.cancel();
        assertEquals(ReservationStatus.CANCELLED, a.getStatus());
        assertFalse(a.overlaps(b));
        assertFalse(b.overlaps(a));
    }

    @Test
    void invalidStatusTransitionsAreRejected() {
        Reservation r = res("A", v1, port1, at(10, 0), at(11, 0));
        r.cancel();
        assertThrows(IllegalStateException.class, () -> r.cancel());
        assertThrows(IllegalStateException.class, () -> r.complete());
        assertThrows(IllegalStateException.class, () -> r.expire());
        assertEquals(ReservationStatus.CANCELLED, r.getStatus());
    }

    @Test
    void completeAndExpireWorkFromActive() {
        Reservation a = res("A", v1, port1, at(10, 0), at(11, 0));
        Reservation b = res("B", v2, port2, at(10, 0), at(11, 0));
        a.complete();
        b.expire();
        assertEquals(ReservationStatus.COMPLETED, a.getStatus());
        assertEquals(ReservationStatus.EXPIRED, b.getStatus());
    }

    @Test
    void isActiveAtUsesHalfOpenInterval() {
        Reservation r = res("A", v1, port1, at(10, 0), at(11, 0));
        assertFalse(r.isActiveAt(at(9, 59)));
        assertTrue(r.isActiveAt(at(10, 0)));
        assertTrue(r.isActiveAt(at(10, 59)));
        assertFalse(r.isActiveAt(at(11, 0)));
        r.cancel();
        assertFalse(r.isActiveAt(at(10, 30)));
    }
}
