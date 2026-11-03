# Smart EV Charging Management System

A Java application that simulates electric-vehicle charging infrastructure: vehicle registration, stations and ports, reservations, charging sessions, and switchable allocation strategies (first-come-first-served, priority-based).

## Requirements

- JDK 17 or newer (`java -version` should show 17+)
- Maven 3.8 or newer (`mvn -version`), or use the Maven built into IntelliJ / Eclipse

## Get the code

```
git clone <repository-url>
cd ev-charging
```

## Build and test

| Command | What it does |
|---|---|
| `mvn compile` | Compiles the code |
| `mvn test` | Runs all JUnit 5 tests |
| `mvn clean` | Deletes the `target/` folder (old build output) |
| `mvn package` | Compiles, tests and builds a runnable jar |

## Run the application

```
mvn compile exec:java
```

or, after `mvn package`:

```
java -jar target/ev-charging-1.0-SNAPSHOT.jar
```

## Project layout

```
ev-charging/
├── pom.xml                  Maven configuration (Java 17, JUnit 5)
├── README.md
├── CONTRIBUTING.md          Branch and teamwork rules
├── .gitignore
└── src/
    ├── main/java/com/team/evcharging/
    │   ├── Main.java        Entry point
    │   ├── common/          Shared enums and exceptions      (Person D)
    │   ├── vehicle/         Vehicle, ElectricVehicle         (Person A)
    │   ├── session/         ChargingSession                  (Person A)
    │   ├── infrastructure/  ChargingStation, ChargingPort    (Person B)
    │   ├── reservation/     Reservation, ReservationService  (Person B)
    │   ├── strategy/        ChargingStrategy + implementations (Person C)
    │   ├── manager/         ChargingManager                  (Person C)
    │   ├── simulation/      Clock, Simulation, scenarios     (Person D)
    │   └── report/          Utilization and session reports  (Person D)
    └── test/java/com/team/evcharging/
        └── (mirror the same packages: one test class per class)
```

## Team

| Person | Area |
|---|---|
| A | Vehicles and charging sessions |
| B | Stations, ports and reservations |
| C | Allocation strategies and ChargingManager |
| D | Simulation, reporting, documentation and setup |

See `CONTRIBUTING.md` for branch rules.

## Demo scenarios

1. Port contention: two vehicles, one port
2. Reservation cancellation
3. First-come-first-served vs priority-based on the same input

(To be completed by Person D.)
