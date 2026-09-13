package com.alphalens.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record QuoteResponse(
        long instrumentId,
        BigDecimal lastPrice,
        BigDecimal change,
        BigDecimal changePercent,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal previousClose,
        long volume,
        Instant asOf,
        String source,
        String market
) {
}
