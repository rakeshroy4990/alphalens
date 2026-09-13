package com.alphalens.service.score;

import com.alphalens.service.analytics.FundamentalAnalyticsService;
import com.alphalens.service.valuation.ValuationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StockScoreServiceTest {

    @Test
    void overallIsWeightedAndComponentsStayVisible() {
        var analytics = new FundamentalAnalyticsService.AnalyticsBundle(
                new BigDecimal("0.20"),
                new BigDecimal("0.15"),
                new BigDecimal("0.25"),
                new BigDecimal("0.12"),
                new BigDecimal("0.18"),
                new BigDecimal("0.16"),
                new BigDecimal("0.80"),
                new BigDecimal("0.40"),
                new BigDecimal("52"),
                BigDecimal.ZERO,
                List.of(),
                List.of(),
                List.of()
        );
        var valuation = new ValuationService.ValuationBundle(
                new BigDecimal("18"),
                new BigDecimal("3"),
                new BigDecimal("10"),
                new BigDecimal("1.2"),
                new BigDecimal("0.4"),
                new ValuationService.DcfResult(null, null, null, "unused")
        );
        var score = StockScoreService.score(analytics, valuation, new BigDecimal("1.5"), null);
        assertThat(score.components()).containsKeys("growth", "quality", "financial", "valuation", "momentum", "risk");
        assertThat(score.weights()).isEqualTo(StockScoreService.DEFAULT_WEIGHTS);
        assertThat(score.overall()).isNotNull();
        assertThat(score.overall()).isBetween(BigDecimal.ZERO, new BigDecimal("100"));
    }
}