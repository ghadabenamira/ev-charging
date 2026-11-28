package com.team.evcharging.reservation;

import com.team.evcharging.infrastructure.ChargingPort;
import com.team.evcharging.infrastructure.ChargingStation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.team.evcharging.session.ChargingSession;
import com.team.evcharging.vehicle.ElectricVehicle;
import com.team.evcharging.vehicle.Vehicle;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceTest {

    private ReservationService service;
    private ChargingStation station;
    private ChargingPort port1;
    private ChargingPort port2;
    private Vehicle v1;
    private Vehicle v2;
    private Vehicle v3;

    private static LocalDateTime at(int hour, int minute) {
        return LocalDateTime.of(2026, 12, 1, hour, minute);
    }

    @BeforeEach
    void setUp() {
        service = new ReservationService();
        station = new ChargingStation("S1", "Lac 2", "Tunis");
        port1 = station.addPort("P1", 22);
        port2 = station.addPort("P2", 50);
        v1 = new ElectricVehicle("V1", "PLATE-V1", "Test EV", 60, 10, 50);
        v2 = new ElectricVehicle("V2", "PLATE-V2", "Test EV", 60, 10, 50);
        v3 = new ElectricVehicle("V3", "PLATE-V3", "Test EV", 60, 10, 50);
    }

    @Test
    void successfulReservation() throws Exception {
        Reservation r = service.createReservation(v1, station, at(10, 0), at(11, 0));
        assertNotNull(r.getId());
        assertSame(port1, r.getPort());
        assertEquals(ReservationStatus.ACTIVE, r.getStatus());
    }

    @Test
    void usesAnotherPortWhenFirstIsBooked() throws Exception {
        Reservation a = service.createReservation(v1, station, at(10, 0), at(11, 0));
        Reservation b = service.createReservation(v2, station, at(10, 30), at(11, 30));
        assertSame(port1, a.getPort());
        assertSame(port2, b.getPort());
    }

    @Test
    void conflictWhenAllPortsAreBooked() throws Exception {
        service.createReservation(v1, station, at(10, 0), at(11, 0));
        service.createReservation(v2, station, at(10, 0), at(11, 0));
        ReservationConflictException ex = assertThrows(ReservationConflictException.class,
                () -> service.createReservation(v3, station, at(10, 30), at(10, 45)));
        assertTrue(ex.getMessage().contains("Lac 2"));
        assertEquals(2, service.getReservationsByStation(station).size());
    }

    @Test
    void adjacentSlotsOnSamePortAreAllowed() throws Exception {
        station = new ChargingStation("S2", "Single", "Tunis");
        station.addPort("P1", 22);
        service.createReservation(v1, station, at(10, 0), at(11, 0));
        Reservation next = service.createReservation(v2, station, at(11, 0), at(12, 0));
        assertEquals(ReservationStatus.ACTIVE, next.getStatus());
    }

    @Test
    void outOfServicePortsAreSkipped() throws Exception {
        port1.setOutOfService();
        Reservation r = service.createReservation(v1, station, at(10, 0), at(11, 0));
        assertSame(port2, r.getPort());
        port2.setOutOfService();
        assertThrows(ReservationConflictException.class,
                () -> service.createReservation(v2, station, at(12, 0), at(13, 0)));
    }

    @Test
    void cancellation() throws Exception {
        Reservation r = service.createReservation(v1, station, at(10, 0), at(11, 0));
        service.cancelReservation(r.getId());
        assertEquals(ReservationStatus.CANCELLED, r.getStatus());
        assertThrows(IllegalStateException.class, () -> service.cancelReservation(r.getId()));
    }

    @Test
    void cancellationDoesNotReleaseAPortInUse() throws Exception {
        Reservation r = service.createReservation(v1, station, at(10, 0), at(11, 0));
        ChargingSession other = new ChargingSession(v3, port1, at(9, 0), 60);
        port1.occupy(other);
        service.cancelReservation(r.getId());
        assertSame(other, port1.getCurrentSession());
        assertFalse(port1.isAvailable());
    }

    @Test
    void bookingAgainAfterCancellation() throws Exception {
        station = new ChargingStation("S2", "Single", "Tunis");
        ChargingPort only = station.addPort("P1", 22);
        Reservation first = service.createReservation(v1, station, at(10, 0), at(11, 0));
        assertThrows(ReservationConflictException.class,
                () -> service.createReservation(v2, station, at(10, 0), at(11, 0)));
        service.cancelReservation(first.getId());
        Reservation second = service.createReservation(v2, station, at(10, 0), at(11, 0));
        assertSame(only, second.getPort());
    }

    @Test
    void retrievalById() throws Exception {
        Reservation r = service.createReservation(v1, station, at(10, 0), at(11, 0));
        assertSame(r, service.getReservation(r.getId()).orElseThrow());
        assertTrue(service.getReservation("RES-9999").isEmpty());
    }

    @Test
    void retrievalByVehicle() throws Exception {
        service.createReservation(v1, station, at(10, 0), at(11, 0));
        service.createReservation(v2, station, at(10, 0), at(11, 0));
        service.createReservation(v1, station, at(12, 0), at(13, 0));
        List<Reservation> mine = service.getReservationsByVehicle(v1);
        assertEquals(2, mine.size());
        assertEquals(1, service.getReservationsByVehicle(v2).size());
        assertTrue(service.getReservationsByVehicle(v3).isEmpty());
    }

    @Test
    void retrievalByStation() throws Exception {
        ChargingStation other = new ChargingStation("S2", "Other", "Sfax");
        other.addPort("P1", 22);
        service.createReservation(v1, station, at(10, 0), at(11, 0));
        service.createReservation(v2, other, at(10, 0), at(11, 0));
        assertEquals(1, service.getReservationsByStation(station).size());
        assertEquals(1, service.getReservationsByStation(other).size());
    }

    @Test
    void returnedCollectionsAreSafe() throws Exception {
        service.createReservation(v1, station, at(10, 0), at(11, 0));
        List<Reservation> list = service.getReservationsByStation(station);
        assertThrows(UnsupportedOperationException.class, () -> list.clear());
        assertEquals(1, service.getReservationsByStation(station).size());
    }

    @Test
    void invalidOrUnknownIdsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.cancelReservation("RES-9999"));
        assertThrows(IllegalArgumentException.class, () -> service.cancelReservation(null));
        assertThrows(IllegalArgumentException.class, () -> service.cancelReservation(" "));
        assertThrows(IllegalArgumentException.class, () -> service.getReservation(null));
    }

    @Test
    void invalidRequestsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.createReservation(null, station, at(10, 0), at(11, 0)));
        assertThrows(IllegalArgumentException.class, () -> service.createReservation(v1, null, at(10, 0), at(11, 0)));
        assertThrows(IllegalArgumentException.class, () -> service.createReservation(v1, station, null, at(11, 0)));
        assertThrows(IllegalArgumentException.class, () -> service.createReservation(v1, station, at(11, 0), at(10, 0)));
        assertThrows(IllegalArgumentException.class, () -> service.createReservation(v1, station, at(10, 0), at(10, 0)));
    }
}
