package com.alphalens.market;

import java.math.BigDecimal;
import java.time.Instant;

public record Quote(
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
        Market market
) {
}
