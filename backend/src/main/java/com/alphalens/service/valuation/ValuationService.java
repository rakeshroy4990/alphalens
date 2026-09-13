package com.alphalens.service.valuation;

import com.alphalens.fundamental.FinancialSnapshot;
import com.alphalens.market.Quote;
import com.alphalens.service.analytics.FundamentalAnalyticsService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class ValuationService {

    private static final int SCALE = 6;

    private ValuationService() {
    }

    public static BigDecimal pe(BigDecimal price, BigDecimal eps) {
        if (price == null || eps == null || eps.signum() <= 0) {
            return null;
        }
        return price.divide(eps, SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal pb(BigDecimal price, BigDecimal bookValuePerShare) {
        if (price == null || bookValuePerShare == null || bookValuePerShare.signum() <= 0) {
            return null;
        }
        return price.divide(bookValuePerShare, SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal evEbitda(BigDecimal price, BigDecimal shares, BigDecimal netDebt, BigDecimal ebitda) {
        if (price == null || shares == null || netDebt == null || ebitda == null || ebitda.signum() <= 0) {
            return null;
        }
        BigDecimal ev = price.multiply(shares).add(netDebt);
        return ev.divide(ebitda, SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal peg(BigDecimal pe, BigDecimal growthDecimal) {
        if (pe == null || growthDecimal == null || growthDecimal.signum() <= 0) {
            return null;
        }
        BigDecimal growthPercent = growthDecimal.multiply(BigDecimal.valueOf(100));
        if (growthPercent.signum() <= 0) {
            return null;
        }
        return pe.divide(growthPercent, SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal percentile(BigDecimal value, List<BigDecimal> history) {
        List<BigDecimal> usable = history.stream().filter(v -> v != null).sorted().toList();
        if (value == null || usable.isEmpty()) {
            return null;
        }
        long below = usable.stream().filter(v -> v.compareTo(value) <= 0).count();
        return BigDecimal.valueOf(below).divide(BigDecimal.valueOf(usable.size()), SCALE, RoundingMode.HALF_UP);
    }

    public static DcfResult dcf(DcfAssumptions assumptions) {
        if (assumptions.discountRate().compareTo(assumptions.terminalGrowth()) <= 0
                || assumptions.fcf().signum() <= 0
                || assumptions.sharesOutstanding().signum() <= 0) {
            return new DcfResult(null, null, assumptions, "DCF not computed: discount rate must exceed terminal growth and FCF/shares must be positive.");
        }
        BigDecimal value = BigDecimal.ZERO;
        BigDecimal fcf = assumptions.fcf();
        for (int year = 1; year <= 5; year++) {
            fcf = fcf.multiply(BigDecimal.ONE.add(assumptions.growth()));
            BigDecimal discount = BigDecimal.ONE.add(assumptions.discountRate()).pow(year);
            value = value.add(fcf.divide(discount, 12, RoundingMode.HALF_UP));
        }
        BigDecimal terminalFcf = fcf.multiply(BigDecimal.ONE.add(assumptions.terminalGrowth()));
        BigDecimal terminalValue = terminalFcf.divide(
                assumptions.discountRate().subtract(assumptions.terminalGrowth()),
                12,
                RoundingMode.HALF_UP
        );
        BigDecimal discountedTerminal = terminalValue.divide(
                BigDecimal.ONE.add(assumptions.discountRate()).pow(5),
                12,
                RoundingMode.HALF_UP
        );
        BigDecimal equityValue = value.add(discountedTerminal).subtract(assumptions.netDebt());
        BigDecimal perShare = equityValue.divide(assumptions.sharesOutstanding(), SCALE, RoundingMode.HALF_UP);
        return new DcfResult(equityValue.setScale(SCALE, RoundingMode.HALF_UP), perShare, assumptions, null);
    }

    public static ValuationBundle value(Quote quote, FinancialSnapshot snapshot, DcfAssumptions assumptions) {
        FinancialSnapshot.Period last = snapshot.periods().isEmpty() ? null : snapshot.periods().get(snapshot.periods().size() - 1);
        BigDecimal eps = last == null ? null : last.eps();
        BigDecimal ebitda = last == null ? null : last.ebitda();
        BigDecimal equity = last == null ? null : last.equity();
        BigDecimal book = (equity == null || assumptions.sharesOutstanding().signum() <= 0)
                ? null
                : equity.divide(assumptions.sharesOutstanding(), SCALE, RoundingMode.HALF_UP);
        BigDecimal pe = pe(quote.lastPrice(), eps);
        var analytics = FundamentalAnalyticsService.analyze(snapshot);
        List<BigDecimal> historicalPe = new ArrayList<>();
        for (FinancialSnapshot.Period period : snapshot.periods()) {
            historicalPe.add(pe(quote.lastPrice(), period.eps()));
        }
        return new ValuationBundle(
                pe,
                pb(quote.lastPrice(), book),
                evEbitda(quote.lastPrice(), assumptions.sharesOutstanding(), assumptions.netDebt(), ebitda),
                peg(pe, analytics.epsCagr()),
                percentile(pe, historicalPe),
                dcf(assumptions)
        );
    }

    public record DcfAssumptions(
            BigDecimal growth,
            BigDecimal terminalGrowth,
            BigDecimal discountRate,
            BigDecimal fcf,
            BigDecimal sharesOutstanding,
            BigDecimal netDebt
    ) {
    }

    public record DcfResult(BigDecimal equityValue, BigDecimal valuePerShare, DcfAssumptions assumptions, String warning) {
    }

    public record ValuationBundle(
            BigDecimal pe,
            BigDecimal pb,
            BigDecimal evEbitda,
            BigDecimal peg,
            BigDecimal historicalPePercentile,
            DcfResult dcf
    ) {
    }
}
