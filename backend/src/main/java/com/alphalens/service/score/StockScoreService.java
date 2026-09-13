package com.alphalens.service.score;

import com.alphalens.service.analytics.FundamentalAnalyticsService;
import com.alphalens.service.valuation.ValuationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

public final class StockScoreService {

    public static final Map<String, BigDecimal> DEFAULT_WEIGHTS = Map.of(
            "growth", new BigDecimal("0.20"),
            "quality", new BigDecimal("0.20"),
            "financial", new BigDecimal("0.15"),
            "valuation", new BigDecimal("0.20"),
            "momentum", new BigDecimal("0.15"),
            "risk", new BigDecimal("0.10")
    );

    private StockScoreService() {
    }

    public static ScoreBundle score(
            FundamentalAnalyticsService.AnalyticsBundle analytics,
            ValuationService.ValuationBundle valuation,
            BigDecimal changePercent,
            Map<String, BigDecimal> weights) {
        Map<String, BigDecimal> resolved = weights == null || weights.isEmpty() ? DEFAULT_WEIGHTS : weights;
        BigDecimal growth = clamp100(scale(analytics.revenueCagr(), new BigDecimal("0.25")));
        BigDecimal quality = average(scale(analytics.roe(), new BigDecimal("0.25")), scale(analytics.roce(), new BigDecimal("0.25")));
        BigDecimal financial = invert(scale(analytics.debtEquity(), new BigDecimal("1.50")));
        BigDecimal valuationScore = invert(scale(valuation.pe(), new BigDecimal("40")));
        BigDecimal momentum = clamp100(new BigDecimal("50").add(nullSafe(changePercent).multiply(new BigDecimal("2"))));
        BigDecimal risk = invert(scale(analytics.promoterPledge(), new BigDecimal("10")));

        Map<String, BigDecimal> components = new LinkedHashMap<>();
        components.put("growth", growth);
        components.put("quality", quality);
        components.put("financial", financial);
        components.put("valuation", valuationScore);
        components.put("momentum", momentum);
        components.put("risk", risk);

        BigDecimal overall = BigDecimal.ZERO;
        BigDecimal weightSum = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : components.entrySet()) {
            BigDecimal weight = resolved.getOrDefault(entry.getKey(), BigDecimal.ZERO);
            if (entry.getValue() != null && weight.signum() > 0) {
                overall = overall.add(entry.getValue().multiply(weight));
                weightSum = weightSum.add(weight);
            }
        }
        BigDecimal total = weightSum.signum() == 0
                ? null
                : overall.divide(weightSum, 2, RoundingMode.HALF_UP);
        return new ScoreBundle(total, Map.copyOf(components), Map.copyOf(resolved));
    }

    private static BigDecimal scale(BigDecimal value, BigDecimal fullMark) {
        if (value == null || fullMark.signum() <= 0) {
            return null;
        }
        return clamp100(value.multiply(new BigDecimal("100")).divide(fullMark, 6, RoundingMode.HALF_UP));
    }

    private static BigDecimal invert(BigDecimal score) {
        if (score == null) {
            return null;
        }
        return clamp100(new BigDecimal("100").subtract(score));
    }

    private static BigDecimal average(BigDecimal a, BigDecimal b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.add(b).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal clamp100(BigDecimal value) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (value.compareTo(new BigDecimal("100")) > 0) {
            return new BigDecimal("100.00");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public record ScoreBundle(
            BigDecimal overall,
            Map<String, BigDecimal> components,
            Map<String, BigDecimal> weights
    ) {
    }
}
