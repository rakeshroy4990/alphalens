package com.alphalens.fundamental;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record FinancialSnapshot(
        long instrumentId,
        String currency,
        String unit,
        String source,
        Instant sourceTimestamp,
        List<Period> periods,
        List<Dividend> dividends
) {
    public record Period(
            String periodType,
            LocalDate periodEnd,
            LocalDate reportedDate,
            BigDecimal revenue,
            BigDecimal ebitda,
            BigDecimal ebit,
            BigDecimal pat,
            BigDecimal eps,
            BigDecimal fcf,
            BigDecimal debt,
            BigDecimal cash,
            BigDecimal equity,
            BigDecimal totalAssets,
            BigDecimal promoterHolding,
            BigDecimal promoterPledge
    ) {
    }

    public record Dividend(LocalDate exDate, BigDecimal amount, String currency) {
    }
}
