package com.alphalens.service;

import com.alphalens.domain.ComponentStatus;
import com.alphalens.domain.HealthSnapshot;
import com.alphalens.repository.HealthRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class HealthService {

    private final HealthRepository healthRepository;
    private final Clock clock;

    public HealthService(HealthRepository healthRepository, Clock clock) {
        this.healthRepository = healthRepository;
        this.clock = clock;
    }

    public HealthSnapshot currentHealth() {
        ComponentStatus database = healthRepository.isDatabaseReachable()
                ? ComponentStatus.UP
                : ComponentStatus.DOWN;
        ComponentStatus status = database == ComponentStatus.UP
                ? ComponentStatus.UP
                : ComponentStatus.DOWN;
        return new HealthSnapshot(status, database, Instant.now(clock));
    }
}
