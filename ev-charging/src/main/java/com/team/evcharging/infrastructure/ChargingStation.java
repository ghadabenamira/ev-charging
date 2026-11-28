package com.team.evcharging.infrastructure;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A charging station that owns and manages its ports (composition). */
public class ChargingStation {

    private final String id;
    private final String name;
    private final String location;
    private final List<ChargingPort> ports = new ArrayList<>();

    public ChargingStation(String id, String name, String location) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Station id must not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Station name must not be null or blank");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Station location must not be null or blank");
        }
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }

    /** Stores a port created by this station. Port ids must be unique within the station. */
    private void addPort(ChargingPort port) {
        if (port == null) {
            throw new IllegalArgumentException("Port must not be null");
        }
        for (ChargingPort existing : ports) {
            if (existing.getId().equals(port.getId())) {
                throw new IllegalArgumentException("Duplicate port id in station " + id + ": " + port.getId());
            }
        }
        ports.add(port);
    }

    /**
     * The only way to create a port: the station builds it and keeps it (composition).
     * Port ids must be unique within the station.
     */
    public ChargingPort addPort(String portId, double powerKw) {
        ChargingPort port = new ChargingPort(portId, powerKw);
        addPort(port);
        return port;
    }

    /** Read-only view; callers cannot add or remove ports through it. */
    public List<ChargingPort> getPorts() {
        return Collections.unmodifiableList(ports);
    }

    /** Ports that can be used right now. */
    public List<ChargingPort> getAvailablePorts() {
        List<ChargingPort> result = new ArrayList<>();
        for (ChargingPort port : ports) {
            if (port.isAvailable()) {
                result.add(port);
            }
        }
        return result;
    }

    /**
     * First port that is physically free for [start, end), or null if none.
     * Out-of-service ports are skipped. Existing reservations are NOT checked here;
     * ReservationService does that.
     */
    public ChargingPort findAvailablePort(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end must not be null");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End must be strictly after start");
        }
        for (ChargingPort port : ports) {
            if (port.isAvailableDuring(start, end)) {
                return port;
            }
        }
        return null;
    }

    public int getTotalPorts() {
        return ports.size();
    }

    @Override
    public String toString() {
        return "ChargingStation{" + id + ", " + name + ", " + location + ", ports=" + ports.size() + "}";
    }
}
