package com.alphalens.fundamental;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic mock statements labeled MOCK. Not live filings.
 */
@Component
public class MockFundamentalCatalog {

    public FinancialSnapshot snapshot(long instrumentId) {
        int seed = (int) (Math.abs(instrumentId) % 7);
        List<FinancialSnapshot.Period> periods = new ArrayList<>();
        for (int year = 0; year < 5; year++) {
            int y = 2021 + year;
            BigDecimal growth = BigDecimal.valueOf(1 + (0.08 + seed * 0.01) * year);
            periods.add(new FinancialSnapshot.Period(
                    "ANNUAL",
                    LocalDate.of(y, 3, 31),
                    LocalDate.of(y, 5, 15),
                    money(1200 + seed * 80, growth),
                    money(280 + seed * 20, growth),
                    money(220 + seed * 15, growth),
                    money(150 + seed * 10, growth),
                    money(18 + seed, growth),
                    money(90 + seed * 8, growth),
                    money(400 + seed * 30, BigDecimal.ONE),
                    money(80 + seed * 5, growth),
                    money(900 + seed * 40, growth),
                    money(1800 + seed * 60, growth),
                    new BigDecimal("51.2500").add(BigDecimal.valueOf(seed)),
                    year == 4 ? new BigDecimal("2.5000") : BigDecimal.ZERO
            ));
        }
        return new FinancialSnapshot(
                instrumentId,
                "INR",
                "CRORE",
                "MOCK",
                Instant.parse("2026-03-31T00:00:00Z"),
                List.copyOf(periods),
                List.of(new FinancialSnapshot.Dividend(LocalDate.of(2025, 8, 12), new BigDecimal("4.50"), "INR"))
        );
    }

    private static BigDecimal money(double base, BigDecimal growth) {
        return BigDecimal.valueOf(base).multiply(growth).setScale(4, java.math.RoundingMode.HALF_UP);
    }
}
