package com.alphalens.service.analytics;

import com.alphalens.fundamental.FinancialSnapshot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Derived fundamental metrics. Null means the input cannot support a reliable value.
 */
public final class FundamentalAnalyticsService {

    private static final int SCALE = 6;

    private FundamentalAnalyticsService() {
    }

    public static BigDecimal cagr(BigDecimal start, BigDecimal end, int periods) {
        if (periods <= 0 || start == null || end == null || start.signum() <= 0 || end.signum() <= 0) {
            return null;
        }
        double ratio = end.doubleValue() / start.doubleValue();
        if (ratio <= 0) {
            return null;
        }
        double value = Math.pow(ratio, 1.0 / periods) - 1.0;
        return BigDecimal.valueOf(value).setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal margin(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.signum() == 0) {
            return null;
        }
        return numerator.divide(denominator, SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal roce(BigDecimal ebit, BigDecimal equity, BigDecimal debt, BigDecimal cash) {
        if (ebit == null || equity == null || debt == null || cash == null) {
            return null;
        }
        BigDecimal capital = equity.add(debt).subtract(cash);
        if (capital.signum() <= 0) {
            return null;
        }
        return ebit.divide(capital, SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal roe(BigDecimal pat, BigDecimal equity) {
        return margin(pat, equity);
    }

    public static BigDecimal debtEquity(BigDecimal debt, BigDecimal equity) {
        return margin(debt, equity);
    }

    public static BigDecimal fcfConversion(BigDecimal fcf, BigDecimal pat) {
        return margin(fcf, pat);
    }

    public static List<BigDecimal> trend(List<FinancialSnapshot.Period> periods, Function<FinancialSnapshot.Period, BigDecimal> extractor) {
        List<BigDecimal> values = periods.stream().map(extractor).toList();
        List<BigDecimal> deltas = new ArrayList<>();
        for (int i = 1; i < values.size(); i++) {
            BigDecimal prev = values.get(i - 1);
            BigDecimal curr = values.get(i);
            if (prev == null || curr == null) {
                deltas.add(null);
            } else {
                deltas.add(curr.subtract(prev));
            }
        }
        return java.util.Collections.unmodifiableList(deltas);
    }

    public static AnalyticsBundle analyze(FinancialSnapshot snapshot) {
        List<FinancialSnapshot.Period> periods = snapshot.periods();
        if (periods.size() < 2) {
            return AnalyticsBundle.insufficient();
        }
        FinancialSnapshot.Period first = periods.get(0);
        FinancialSnapshot.Period last = periods.get(periods.size() - 1);
        int years = periods.size() - 1;
        return new AnalyticsBundle(
                cagr(first.revenue(), last.revenue(), years),
                cagr(first.eps(), last.eps(), years),
                margin(last.ebitda(), last.revenue()),
                margin(last.pat(), last.revenue()),
                roe(last.pat(), last.equity()),
                roce(last.ebit(), last.equity(), last.debt(), last.cash()),
                fcfConversion(last.fcf(), last.pat()),
                debtEquity(last.debt(), last.equity()),
                last.promoterHolding(),
                last.promoterPledge(),
                trend(periods, FinancialSnapshot.Period::debt),
                trend(periods, FinancialSnapshot.Period::pat),
                trend(periods, FinancialSnapshot.Period::promoterHolding)
        );
    }

    public record AnalyticsBundle(
            BigDecimal revenueCagr,
            BigDecimal epsCagr,
            BigDecimal ebitdaMargin,
            BigDecimal patMargin,
            BigDecimal roe,
            BigDecimal roce,
            BigDecimal fcfConversion,
            BigDecimal debtEquity,
            BigDecimal promoterHolding,
            BigDecimal promoterPledge,
            List<BigDecimal> debtTrend,
            List<BigDecimal> marginTrend,
            List<BigDecimal> promoterTrend
    ) {
        static AnalyticsBundle insufficient() {
            return new AnalyticsBundle(null, null, null, null, null, null, null, null, null, null, List.of(), List.of(), List.of());
        }
    }
}
