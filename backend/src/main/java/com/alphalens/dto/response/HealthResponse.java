package com.alphalens.dto.response;

import com.alphalens.domain.ComponentStatus;

import java.time.Instant;

public record HealthResponse(
        ComponentStatus status,
        ComponentStatus database,
        String service,
        Instant timestamp
) {
}
