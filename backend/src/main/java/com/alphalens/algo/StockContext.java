package com.alphalens.algo;

import java.math.BigDecimal;
import java.util.Map;

public record StockContext(long instrumentId, Map<String, BigDecimal> metrics) {

    public BigDecimal metric(String name) {
        return metrics.get(name);
    }
}
