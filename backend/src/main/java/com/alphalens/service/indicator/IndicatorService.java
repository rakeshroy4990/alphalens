package com.alphalens.service.indicator;

import com.alphalens.exception.InvalidRequestException;
import com.alphalens.market.Candle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deterministic technical indicators. Null means insufficient or invalid input — never a fabricated value.
 */
public final class IndicatorService {

    private static final int SCALE = 6;

    private IndicatorService() {
    }

    public static List<BigDecimal> sma(List<BigDecimal> values, int period) {
        requirePeriod(period);
        List<BigDecimal> out = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            if (i + 1 < period || hasNull(values, i - period + 1, i)) {
                out.add(null);
                continue;
            }
            BigDecimal sum = BigDecimal.ZERO;
            for (int j = i - period + 1; j <= i; j++) {
                sum = sum.add(values.get(j));
            }
            out.add(sum.divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP));
        }
        return Collections.unmodifiableList(out);
    }

    public static List<BigDecimal> ema(List<BigDecimal> values, int period) {
        requirePeriod(period);
        List<BigDecimal> out = new ArrayList<>(values.size());
        BigDecimal multiplier = BigDecimal.valueOf(2).divide(BigDecimal.valueOf(period + 1L), 12, RoundingMode.HALF_UP);
        BigDecimal previous = null;
        for (int i = 0; i < values.size(); i++) {
            BigDecimal value = values.get(i);
            if (value == null) {
                out.add(null);
                previous = null;
                continue;
            }
            if (previous == null) {
                if (i + 1 < period || hasNull(values, i - period + 1, i)) {
                    out.add(null);
                    continue;
                }
                BigDecimal seed = sma(values.subList(i - period + 1, i + 1), period).get(period - 1);
                out.add(seed);
                previous = seed;
                continue;
            }
            BigDecimal next = value.subtract(previous).multiply(multiplier).add(previous).setScale(SCALE, RoundingMode.HALF_UP);
            out.add(next);
            previous = next;
        }
        return Collections.unmodifiableList(out);
    }

    public static List<BigDecimal> rsi(List<BigDecimal> closes, int period) {
        requirePeriod(period);
        List<BigDecimal> out = new ArrayList<>(closes.size());
        for (int i = 0; i < closes.size(); i++) {
            out.add(null);
        }
        if (closes.size() <= period) {
            return Collections.unmodifiableList(out);
        }
        BigDecimal avgGain = BigDecimal.ZERO;
        BigDecimal avgLoss = BigDecimal.ZERO;
        for (int i = 1; i <= period; i++) {
            if (closes.get(i) == null || closes.get(i - 1) == null) {
                return Collections.unmodifiableList(out);
            }
            BigDecimal change = closes.get(i).subtract(closes.get(i - 1));
            if (change.signum() >= 0) {
                avgGain = avgGain.add(change);
            } else {
                avgLoss = avgLoss.add(change.abs());
            }
        }
        avgGain = avgGain.divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);
        avgLoss = avgLoss.divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);
        out.set(period, rsiFromAverages(avgGain, avgLoss));
        for (int i = period + 1; i < closes.size(); i++) {
            if (closes.get(i) == null || closes.get(i - 1) == null) {
                out.set(i, null);
                continue;
            }
            BigDecimal change = closes.get(i).subtract(closes.get(i - 1));
            BigDecimal gain = change.signum() > 0 ? change : BigDecimal.ZERO;
            BigDecimal loss = change.signum() < 0 ? change.abs() : BigDecimal.ZERO;
            avgGain = avgGain.multiply(BigDecimal.valueOf(period - 1L)).add(gain)
                    .divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);
            avgLoss = avgLoss.multiply(BigDecimal.valueOf(period - 1L)).add(loss)
                    .divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);
            out.set(i, rsiFromAverages(avgGain, avgLoss));
        }
        return Collections.unmodifiableList(out);
    }

    public static MacdSeries macd(List<BigDecimal> closes, int fast, int slow, int signal) {
        List<BigDecimal> fastEma = ema(closes, fast);
        List<BigDecimal> slowEma = ema(closes, slow);
        List<BigDecimal> macdLine = new ArrayList<>(closes.size());
        for (int i = 0; i < closes.size(); i++) {
            if (fastEma.get(i) == null || slowEma.get(i) == null) {
                macdLine.add(null);
            } else {
                macdLine.add(fastEma.get(i).subtract(slowEma.get(i)).setScale(SCALE, RoundingMode.HALF_UP));
            }
        }
        List<BigDecimal> signalLine = ema(macdLine, signal);
        List<BigDecimal> histogram = new ArrayList<>(closes.size());
        for (int i = 0; i < closes.size(); i++) {
            if (macdLine.get(i) == null || signalLine.get(i) == null) {
                histogram.add(null);
            } else {
                histogram.add(macdLine.get(i).subtract(signalLine.get(i)).setScale(SCALE, RoundingMode.HALF_UP));
            }
        }
        return new MacdSeries(
                Collections.unmodifiableList(macdLine),
                Collections.unmodifiableList(signalLine),
                Collections.unmodifiableList(histogram)
        );
    }

    public static List<BigDecimal> vwap(List<Candle> candles) {
        List<BigDecimal> out = new ArrayList<>(candles.size());
        BigDecimal cumPv = BigDecimal.ZERO;
        BigDecimal cumVol = BigDecimal.ZERO;
        for (Candle candle : candles) {
            if (candle == null || candle.volume() <= 0) {
                out.add(null);
                continue;
            }
            BigDecimal typical = candle.high().add(candle.low()).add(candle.close())
                    .divide(BigDecimal.valueOf(3), SCALE, RoundingMode.HALF_UP);
            cumPv = cumPv.add(typical.multiply(BigDecimal.valueOf(candle.volume())));
            cumVol = cumVol.add(BigDecimal.valueOf(candle.volume()));
            out.add(cumPv.divide(cumVol, SCALE, RoundingMode.HALF_UP));
        }
        return Collections.unmodifiableList(out);
    }

    public static BollingerSeries bollinger(List<BigDecimal> values, int period, BigDecimal multiplier) {
        List<BigDecimal> middle = sma(values, period);
        List<BigDecimal> upper = new ArrayList<>(values.size());
        List<BigDecimal> lower = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            if (middle.get(i) == null) {
                upper.add(null);
                lower.add(null);
                continue;
            }
            BigDecimal mean = middle.get(i);
            BigDecimal variance = BigDecimal.ZERO;
            for (int j = i - period + 1; j <= i; j++) {
                BigDecimal diff = values.get(j).subtract(mean);
                variance = variance.add(diff.multiply(diff));
            }
            BigDecimal stdev = sqrt(variance.divide(BigDecimal.valueOf(period), 12, RoundingMode.HALF_UP));
            BigDecimal band = stdev.multiply(multiplier).setScale(SCALE, RoundingMode.HALF_UP);
            upper.add(mean.add(band));
            lower.add(mean.subtract(band));
        }
        return new BollingerSeries(middle, Collections.unmodifiableList(upper), Collections.unmodifiableList(lower));
    }

    public record MacdSeries(List<BigDecimal> macd, List<BigDecimal> signal, List<BigDecimal> histogram) {
    }

    public record BollingerSeries(List<BigDecimal> middle, List<BigDecimal> upper, List<BigDecimal> lower) {
    }

    private static BigDecimal rsiFromAverages(BigDecimal avgGain, BigDecimal avgLoss) {
        if (avgLoss.signum() == 0) {
            return avgGain.signum() == 0 ? new BigDecimal("50.000000") : new BigDecimal("100.000000");
        }
        BigDecimal rs = avgGain.divide(avgLoss, SCALE, RoundingMode.HALF_UP);
        return new BigDecimal("100").subtract(
                new BigDecimal("100").divide(BigDecimal.ONE.add(rs), SCALE, RoundingMode.HALF_UP)
        );
    }

    private static void requirePeriod(int period) {
        if (period < 2) {
            throw new InvalidRequestException("period must be >= 2");
        }
    }

    private static boolean hasNull(List<BigDecimal> values, int from, int to) {
        int start = Math.max(from, 0);
        for (int i = start; i <= to; i++) {
            if (values.get(i) == null) {
                return true;
            }
        }
        return false;
    }

    private static BigDecimal sqrt(BigDecimal value) {
        if (value.signum() == 0) {
            return BigDecimal.ZERO;
        }
        double raw = Math.sqrt(value.doubleValue());
        return BigDecimal.valueOf(raw).setScale(SCALE, RoundingMode.HALF_UP);
    }
}
