package com.alphalens.service.broker;

import com.alphalens.exception.InvalidRequestException;
import com.alphalens.integration.broker.BrokerProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class BrokerService {

    private final BrokerProvider brokerProvider;
    private final JdbcTemplate jdbcTemplate;
    private final ConcurrentHashMap<UUID, Instant> lastSync = new ConcurrentHashMap<>();
    private final AtomicReference<Map<String, Object>> cache = new AtomicReference<>();

    public BrokerService(BrokerProvider brokerProvider, JdbcTemplate jdbcTemplate) {
        this.brokerProvider = brokerProvider;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> connect(UUID userId, boolean consent) {
        if (!consent) {
            throw new InvalidRequestException("Explicit consent is required before connecting a broker");
        }
        jdbcTemplate.update(
                """
                        INSERT INTO broker_connections (user_id, provider, consent, status)
                        VALUES (?, ?, TRUE, 'PENDING')
                        """,
                userId,
                brokerProvider.name()
        );
        return brokerProvider.status(true);
    }

    public Map<String, Object> login() {
        return brokerProvider.loginUrl();
    }

    public Map<String, Object> sync(UUID userId, boolean force) {
        Instant previous = lastSync.get(userId);
        if (!force && previous != null && Instant.now().minusSeconds(60).isBefore(previous)) {
            Map<String, Object> cached = cache.get();
            if (cached != null) {
                return Map.of("cached", true, "snapshot", cached, "nextAllowedAfterSeconds", 60);
            }
        }
        Map<String, Object> snapshot = Map.of(
                "provider", brokerProvider.name(),
                "holdings", brokerProvider.holdings(),
                "positions", brokerProvider.positions(),
                "orders", brokerProvider.orders(),
                "trades", brokerProvider.trades(),
                "syncedAt", Instant.now()
        );
        cache.set(snapshot);
        lastSync.put(userId, Instant.now());
        return Map.of("cached", false, "snapshot", snapshot);
    }
}
