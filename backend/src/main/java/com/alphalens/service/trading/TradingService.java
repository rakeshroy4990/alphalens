package com.alphalens.service.trading;

import com.alphalens.exception.InvalidRequestException;
import com.alphalens.service.InstrumentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TradingService {

    private final InstrumentService instrumentService;
    private final ConcurrentHashMap<String, Map<String, Object>> previews = new ConcurrentHashMap<>();

    public TradingService(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    public Map<String, Object> preview(UUID userId, long instrumentId, String side, BigDecimal quantity, BigDecimal price) {
        instrumentService.require(instrumentId);
        if (quantity == null || quantity.signum() <= 0) {
            throw new InvalidRequestException("quantity must be positive");
        }
        String confirmationId = userId + ":" + instrumentId + ":" + Instant.now().toEpochMilli();
        Map<String, Object> preview = new java.util.LinkedHashMap<>();
        preview.put("confirmationId", confirmationId);
        preview.put("instrumentId", instrumentId);
        preview.put("side", side == null ? "BUY" : side.toUpperCase());
        preview.put("quantity", quantity);
        preview.put("price", price);
        preview.put("status", "REQUIRES_CONFIRMATION");
        preview.put("warnings", java.util.List.of(
                "Orders are never placed automatically from algorithm signals.",
                "Confirm explicitly to submit. Broker is not live without Kite credentials."
        ));
        previews.put(confirmationId, preview);
        return preview;
    }

    public Map<String, Object> confirm(String confirmationId) {
        Map<String, Object> preview = previews.remove(confirmationId);
        if (preview == null) {
            throw new InvalidRequestException("Unknown or expired confirmation");
        }
        return Map.of(
                "orderId", "SIM-" + confirmationId.hashCode(),
                "status", "SIMULATED_REJECTED",
                "reason", "Live Kite order routing is disabled until a server-side session exists.",
                "preview", preview
        );
    }

    public Map<String, Object> cancel(String orderId) {
        return Map.of("orderId", orderId, "status", "CANCEL_NOT_LIVE");
    }
}
