package com.alphalens.service.analytics;

import com.alphalens.fundamental.FinancialSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FundamentalAnalyticsServiceTest {

    @Test
    void cagrIsTenPercentFor121Over100AcrossTwoPeriods() {
        assertThat(FundamentalAnalyticsService.cagr(new BigDecimal("100"), new BigDecimal("121"), 2))
                .isEqualByComparingTo("0.100000");
    }

    @Test
    void cagrIsNullWhenStartIsZeroOrNegative() {
        assertThat(FundamentalAnalyticsService.cagr(BigDecimal.ZERO, new BigDecimal("10"), 2)).isNull();
        assertThat(FundamentalAnalyticsService.cagr(new BigDecimal("-5"), new BigDecimal("10"), 2)).isNull();
    }

    @Test
    void marginIsNullOnZeroDenominator() {
        assertThat(FundamentalAnalyticsService.margin(new BigDecimal("10"), BigDecimal.ZERO)).isNull();
    }

    @Test
    void analyzeDoesNotInventValuesForSinglePeriod() {
        FinancialSnapshot snapshot = new FinancialSnapshot(
                1L,
                "INR",
                "CRORE",
                "MOCK",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(period(new BigDecimal("100"), new BigDecimal("10"))),
                List.of()
        );
        var analytics = FundamentalAnalyticsService.analyze(snapshot);
        assertThat(analytics.revenueCagr()).isNull();
        assertThat(analytics.debtTrend()).isEmpty();
    }

    private static FinancialSnapshot.Period period(BigDecimal revenue, BigDecimal pat) {
        return new FinancialSnapshot.Period(
                "ANNUAL",
                LocalDate.of(2025, 3, 31),
                LocalDate.of(2025, 5, 1),
                revenue,
                new BigDecimal("20"),
                new BigDecimal("15"),
                pat,
                new BigDecimal("2"),
                new BigDecimal("8"),
                new BigDecimal("40"),
                new BigDecimal("5"),
                new BigDecimal("80"),
                new BigDecimal("150"),
                new BigDecimal("50"),
                BigDecimal.ZERO
        );
    }
}
