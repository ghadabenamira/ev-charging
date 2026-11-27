package com.team.evcharging.session;

/** Minimal view of a charging port, so sessions don't depend on the port class yet. */
public interface PortInfo {
    double getPowerKw();
}