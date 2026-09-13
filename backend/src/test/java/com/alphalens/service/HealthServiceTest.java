package com.alphalens.service;

import com.alphalens.domain.ComponentStatus;
import com.alphalens.domain.HealthSnapshot;
import com.alphalens.repository.HealthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-09-13T09:30:00Z");

    private HealthRepository healthRepository;
    private HealthService healthService;

    @BeforeEach
    void setUp() {
        healthRepository = mock(HealthRepository.class);
        healthService = new HealthService(healthRepository, Clock.fixed(FIXED_NOW, ZoneOffset.UTC));
    }

    @Test
    void reportsUpWhenDatabaseIsReachable() {
        when(healthRepository.isDatabaseReachable()).thenReturn(true);

        HealthSnapshot snapshot = healthService.currentHealth();

        assertThat(snapshot.status()).isEqualTo(ComponentStatus.UP);
        assertThat(snapshot.database()).isEqualTo(ComponentStatus.UP);
        assertThat(snapshot.timestamp()).isEqualTo(FIXED_NOW);
    }

    @Test
    void reportsDownWhenDatabaseIsUnreachable() {
        when(healthRepository.isDatabaseReachable()).thenReturn(false);

        HealthSnapshot snapshot = healthService.currentHealth();

        assertThat(snapshot.status()).isEqualTo(ComponentStatus.DOWN);
        assertThat(snapshot.database()).isEqualTo(ComponentStatus.DOWN);
        assertThat(snapshot.timestamp()).isEqualTo(FIXED_NOW);
    }
}
