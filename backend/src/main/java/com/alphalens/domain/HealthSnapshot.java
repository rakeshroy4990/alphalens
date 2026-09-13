package com.alphalens.domain;

import java.time.Instant;

public record HealthSnapshot(
        ComponentStatus status,
        ComponentStatus database,
        Instant timestamp
) {
}
