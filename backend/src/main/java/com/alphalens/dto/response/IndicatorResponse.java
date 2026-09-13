package com.alphalens.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record IndicatorResponse(
        long instrumentId,
        String timeframe,
        String source,
        List<BigDecimal> sma20,
        List<BigDecimal> ema20,
        List<BigDecimal> rsi14,
        List<BigDecimal> macd,
        List<BigDecimal> macdSignal,
        List<BigDecimal> macdHistogram,
        List<BigDecimal> vwap,
        List<BigDecimal> bollingerMiddle,
        List<BigDecimal> bollingerUpper,
        List<BigDecimal> bollingerLower
) {
}
