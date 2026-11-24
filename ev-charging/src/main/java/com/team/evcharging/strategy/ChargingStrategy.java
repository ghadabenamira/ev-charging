
package com.team.evcharging.strategy;

import java.util.List;
import java.time.LocalDateTime;

public interface ChargingStrategy {

    ChargingRequest selectNext(
        List<ChargingRequest> waiting,
        LocalDateTime now
    );

    String getName();
}
