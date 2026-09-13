package com.alphalens.dto.response;

import java.time.Instant;
import java.util.List;

public record CandleSeriesResponse(
        long instrumentId,
        String timeframe,
        Instant from,
        Instant to,
        String source,
        List<CandleResponse> candles
) {
}
