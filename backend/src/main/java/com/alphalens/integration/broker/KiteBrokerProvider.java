package com.alphalens.integration.broker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Kite is a broker adapter only. Tokens stay server-side. No live calls without configured keys.
 */
@Component
public class KiteBrokerProvider implements BrokerProvider {

    private final String apiKeyConfigured;

    public KiteBrokerProvider(@Value("${KITE_API_KEY:}") String apiKeyConfigured) {
        this.apiKeyConfigured = apiKeyConfigured;
    }

    @Override
    public String name() {
        return "KITE";
    }

    @Override
    public Map<String, Object> loginUrl() {
        if (apiKeyConfigured == null || apiKeyConfigured.isBlank()) {
            return Map.of(
                    "provider", name(),
                    "configured", false,
                    "message", "Kite API key is not configured. Tokens are never sent to the browser."
            );
        }
        return Map.of(
                "provider", name(),
                "configured", true,
                "loginPath", "/api/broker/kite/login",
                "message", "Complete Kite login on the server. api_secret and access_token stay on the backend."
        );
    }

    @Override
    public Map<String, Object> status(boolean consent) {
        return Map.of(
                "provider", name(),
                "consent", consent,
                "connected", false,
                "reason", consent ? "Waiting for Kite session (not live in this environment)." : "Explicit consent required."
        );
    }

    @Override
    public List<Map<String, Object>> holdings() {
        return List.of();
    }

    @Override
    public List<Map<String, Object>> positions() {
        return List.of();
    }

    @Override
    public List<Map<String, Object>> orders() {
        return List.of();
    }

    @Override
    public List<Map<String, Object>> trades() {
        return List.of();
    }
}
