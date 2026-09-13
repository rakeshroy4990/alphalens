package com.alphalens.identity;

import java.util.UUID;

public final class DemoUsers {

    public static final UUID DEFAULT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private DemoUsers() {
    }

    public static UUID resolve(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return DEFAULT_ID;
        }
        try {
            return UUID.fromString(headerValue.trim());
        } catch (IllegalArgumentException ex) {
            return DEFAULT_ID;
        }
    }
}
