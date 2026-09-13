package com.alphalens.service.indicator;

import com.alphalens.market.Candle;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class IndicatorServiceTest {

    @Test
    void smaUsesKnownWindowAverageAndLeavesWarmupNull() {
        List<BigDecimal> values = decimals(1, 2, 3, 4, 5);
        List<BigDecimal> sma = IndicatorService.sma(values, 3);
        assertThat(sma.get(0)).isNull();
        assertThat(sma.get(1)).isNull();
        assertThat(sma.get(2)).isEqualByComparingTo("2.000000");
        assertThat(sma.get(3)).isEqualByComparingTo("3.000000");
        assertThat(sma.get(4)).isEqualByComparingTo("4.000000");
    }

    @Test
    void rsiIsHighForStrictlyRisingPrices() {
        List<BigDecimal> closes = IntStream.rangeClosed(1, 20)
                .mapToObj(i -> BigDecimal.valueOf(i))
                .toList();
        List<BigDecimal> rsi = IndicatorService.rsi(closes, 14);
        assertThat(rsi.get(14)).isEqualByComparingTo("100.000000");
        assertThat(rsi.get(rsi.size() - 1)).isEqualByComparingTo("100.000000");
    }

    @Test
    void vwapIsVolumeWeightedTypicalPrice() {
        List<Candle> candles = List.of(
                candle(10, 12, 8, 11, 100),
                candle(11, 13, 10, 12, 300)
        );
        List<BigDecimal> vwap = IndicatorService.vwap(candles);
        assertThat(vwap.get(0)).isEqualByComparingTo("10.333333");
        assertThat(vwap.get(1)).isEqualByComparingTo("11.333334");
    }

    @Test
    void bollingerBandsAreSymmetricAroundSma() {
        List<BigDecimal> values = decimals(2, 4, 4, 4, 6);
        var bands = IndicatorService.bollinger(values, 5, new BigDecimal("2"));
        assertThat(bands.middle().get(4)).isEqualByComparingTo("4.000000");
        assertThat(bands.upper().get(4).subtract(bands.middle().get(4)))
                .isEqualByComparingTo(bands.middle().get(4).subtract(bands.lower().get(4)));
    }

    private static List<BigDecimal> decimals(int... values) {
        return IntStream.of(values).mapToObj(BigDecimal::valueOf).toList();
    }

    private static Candle candle(int open, int high, int low, int close, long volume) {
        return new Candle(
                Instant.parse("2026-01-01T00:00:00Z"),
                BigDecimal.valueOf(open),
                BigDecimal.valueOf(high),
                BigDecimal.valueOf(low),
                BigDecimal.valueOf(close),
                volume
        );
    }
}
