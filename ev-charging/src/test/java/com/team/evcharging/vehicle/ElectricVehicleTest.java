package com.team.evcharging.vehicle;

class ElectricVehicleTest {

    private void assertIllegalArgument(Runnable action) {
        boolean rejected = false;
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        assertTrue(rejected);
    }

    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }

    private void assertEquals(double expected, double actual, double tolerance) {
        if (Math.abs(expected - actual) > tolerance) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }

    private ElectricVehicle car(double capacity, double charge) {
        return new ElectricVehicle("V1", "AB-123-CD", "Zoe", capacity, charge, 22);
    }

    void addChargeBeyondCapacityIsCapped() {
        ElectricVehicle v = car(50, 40);
        double added = v.addCharge(30);
        assertEquals(10, added, 1e-9);
        assertEquals(50, v.getCurrentChargeKwh(), 1e-9);
        assertTrue(v.isFull());
    }

    void stateOfChargeIsAPercentage() {
        assertEquals(50.0, car(60, 30).getStateOfCharge(), 1e-9);
    }

    void invalidCapacityOrChargeIsRejected() {
        assertIllegalArgument(() -> car(0, 0));
        assertIllegalArgument(() -> car(-5, 0));
        assertIllegalArgument(() -> car(50, -1));
        assertIllegalArgument(() -> car(50, 51));
    }

    void invalidMaxPowerIsRejected() {
        assertIllegalArgument(() -> new ElectricVehicle("V1", "AB", "Zoe", 50, 10, 0));
    }

    void negativeEnergyIsRejected() {
        boolean rejected = false;
        try {
            car(50, 10).addCharge(-1);
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        assertTrue(rejected);
    }

    public static void main(String[] args) {
        ElectricVehicleTest test = new ElectricVehicleTest();
        test.addChargeBeyondCapacityIsCapped();
        test.stateOfChargeIsAPercentage();
        test.invalidCapacityOrChargeIsRejected();
        test.invalidMaxPowerIsRejected();
        test.negativeEnergyIsRejected();
    }
}