package com.alphalens.market;

import java.time.Instant;
import java.util.List;

public record HistoricalData(
        long instrumentId,
        Timeframe timeframe,
        Instant from,
        Instant to,
        String source,
        List<Candle> candles
) {
}
