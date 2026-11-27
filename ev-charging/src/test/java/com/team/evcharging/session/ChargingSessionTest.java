package com.team.evcharging.session;

import static org.junit.jupiter.api.Assertions.*;

import com.team.evcharging.vehicle.ElectricVehicle;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ChargingSessionTest {
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 8, 0);

    private ElectricVehicle car(double capacity, double charge, double maxPower) {
        return new ElectricVehicle("V1", "AB-123-CD", "Zoe", capacity, charge, maxPower);
    }

    @Test
    void sessionStopsAtTargetLevel() {
        ElectricVehicle v = car(50, 40, 22);
        ChargingSession s = new ChargingSession(v, () -> 22, T0, 50);
        double added = s.tick(60);
        assertEquals(10, added, 1e-6);
        assertEquals(SessionStatus.COMPLETED, s.getStatus());
        assertEquals(50, v.getCurrentChargeKwh(), 1e-6);
    }

    @Test
    void slowerOfPortAndVehicleWins() {
        ElectricVehicle v = car(60, 0, 22);
        ChargingSession s = new ChargingSession(v, () -> 50, T0, 60);
        assertEquals(22, s.tick(60), 1e-6);
    }

    @Test
    void fullVehicleCompletesImmediately() {
        ElectricVehicle v = car(50, 50, 22);
        ChargingSession s = new ChargingSession(v, () -> 22, T0, 50);
        assertTrue(s.isComplete());
        assertEquals(0, s.getEnergyDelivered(), 1e-9);
    }

    @Test
    void stopEarlyKeepsChargeAndInterrupts() {
        ElectricVehicle v = car(60, 0, 22);
        ChargingSession s = new ChargingSession(v, () -> 22, T0, 60);
        s.tick(30);
        s.stop();
        assertEquals(SessionStatus.INTERRUPTED, s.getStatus());
        assertEquals(11, v.getCurrentChargeKwh(), 1e-6);
        assertEquals(0, s.tick(30), 1e-9);   // no more energy after stop
    }
}