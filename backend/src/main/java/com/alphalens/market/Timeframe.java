package com.alphalens.market;

import com.alphalens.exception.InvalidRequestException;

import java.time.Duration;
import java.util.Locale;

public enum Timeframe {
    M1(Duration.ofMinutes(1), 400),
    M5(Duration.ofMinutes(5), 400),
    M15(Duration.ofMinutes(15), 400),
    H1(Duration.ofHours(1), 500),
    D1(Duration.ofDays(1), 2000),
    W1(Duration.ofDays(7), 520);

    private final Duration barDuration;
    private final int maxBars;

    Timeframe(Duration barDuration, int maxBars) {
        this.barDuration = barDuration;
        this.maxBars = maxBars;
    }

    public Duration barDuration() {
        return barDuration;
    }

    public int maxBars() {
        return maxBars;
    }

    public static Timeframe fromApi(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidRequestException("timeframe is required");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "1M", "M1" -> M1;
            case "5M", "M5" -> M5;
            case "15M", "M15" -> M15;
            case "1H", "H1" -> H1;
            case "1D", "D1" -> D1;
            case "1W", "W1" -> W1;
            default -> throw new InvalidRequestException("Unsupported timeframe: " + raw);
        };
    }

    public String apiCode() {
        return switch (this) {
            case M1 -> "1M";
            case M5 -> "5M";
            case M15 -> "15M";
            case H1 -> "1H";
            case D1 -> "1D";
            case W1 -> "1W";
        };
    }
}
