package com.alphalens.integration.market;

import com.alphalens.market.Candle;
import com.alphalens.market.HistoricalData;
import com.alphalens.market.Market;
import com.alphalens.market.Quote;
import com.alphalens.market.Timeframe;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic mock quotes and candles. Labeled MOCK — not live market data.
 */
@Component
public class MockMarketDataProvider implements MarketDataProvider {

    @Override
    public String sourceName() {
        return "MOCK";
    }

    @Override
    public Quote quote(long instrumentId) {
        HistoricalData day = historical(
                instrumentId,
                Timeframe.D1,
                Instant.now().minus(3, ChronoUnit.DAYS),
                Instant.now()
        );
        List<Candle> candles = day.candles();
        Candle last = candles.get(candles.size() - 1);
        Candle prev = candles.size() > 1 ? candles.get(candles.size() - 2) : last;
        BigDecimal change = last.close().subtract(prev.close());
        BigDecimal changePercent = prev.close().signum() == 0
                ? BigDecimal.ZERO
                : change.multiply(BigDecimal.valueOf(100)).divide(prev.close(), 4, RoundingMode.HALF_UP);
        return new Quote(
                instrumentId,
                last.close(),
                change,
                changePercent,
                last.open(),
                last.high(),
                last.low(),
                prev.close(),
                last.volume(),
                last.timestamp(),
                sourceName(),
                Market.MOCK
        );
    }

    @Override
    public HistoricalData historical(long instrumentId, Timeframe timeframe, Instant from, Instant to) {
        if (!from.isBefore(to) && !from.equals(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
        Instant cursor = align(from, timeframe);
        Instant end = to;
        List<Candle> candles = new ArrayList<>();
        BigDecimal price = basePrice(instrumentId);
        int index = 0;
        while (!cursor.isAfter(end) && candles.size() < timeframe.maxBars()) {
            double wave = Math.sin((instrumentId * 13 + index) * 0.17);
            double drift = index * 0.012;
            BigDecimal open = money(price.doubleValue() * (1 + wave * 0.008 + drift * 0.001));
            BigDecimal close = money(open.doubleValue() * (1 + Math.cos((instrumentId + index) * 0.11) * 0.01));
            BigDecimal high = money(Math.max(open.doubleValue(), close.doubleValue()) * 1.008);
            BigDecimal low = money(Math.min(open.doubleValue(), close.doubleValue()) * 0.992);
            long volume = 80_000L + Math.abs((instrumentId * 997 + index * 131) % 240_000);
            candles.add(new Candle(cursor, open, high, low, close, volume));
            price = close;
            cursor = cursor.plus(timeframe.barDuration());
            index++;
        }
        return new HistoricalData(instrumentId, timeframe, from, to, sourceName(), List.copyOf(candles));
    }

    static BigDecimal basePrice(long instrumentId) {
        double raw = 80 + (Math.abs(instrumentId * 37) % 920);
        return money(raw);
    }

    private static Instant align(Instant from, Timeframe timeframe) {
        long seconds = timeframe.barDuration().getSeconds();
        long epoch = from.getEpochSecond();
        return Instant.ofEpochSecond(epoch - (epoch % seconds));
    }

    private static BigDecimal money(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
