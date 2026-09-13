package com.alphalens.service;

import com.alphalens.dto.response.CandleResponse;
import com.alphalens.dto.response.CandleSeriesResponse;
import com.alphalens.exception.InvalidRequestException;
import com.alphalens.integration.market.MarketDataProvider;
import com.alphalens.market.Candle;
import com.alphalens.market.HistoricalData;
import com.alphalens.market.Timeframe;
import com.alphalens.repository.CandleJdbcRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class CandleService {

    private final InstrumentService instrumentService;
    private final MarketDataProvider marketDataProvider;
    private final CandleJdbcRepository candleJdbcRepository;

    public CandleService(
            InstrumentService instrumentService,
            MarketDataProvider marketDataProvider,
            CandleJdbcRepository candleJdbcRepository) {
        this.instrumentService = instrumentService;
        this.marketDataProvider = marketDataProvider;
        this.candleJdbcRepository = candleJdbcRepository;
    }

    public CandleSeriesResponse series(long instrumentId, String timeframeRaw, LocalDate fromDate, LocalDate toDate) {
        instrumentService.require(instrumentId);
        Timeframe timeframe = Timeframe.fromApi(timeframeRaw);
        if (fromDate == null || toDate == null) {
            throw new InvalidRequestException("from and to are required");
        }
        Instant from = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = toDate.plusDays(1).atStartOfDay().minusNanos(1).toInstant(ZoneOffset.UTC);
        if (from.isAfter(to)) {
            throw new InvalidRequestException("from must be on or before to");
        }
        long maxSpan = timeframe.barDuration().multipliedBy(timeframe.maxBars()).getSeconds();
        if (to.getEpochSecond() - from.getEpochSecond() > maxSpan) {
            throw new InvalidRequestException("Requested range exceeds " + timeframe.maxBars() + " bars for " + timeframe.apiCode());
        }

        List<Candle> stored = candleJdbcRepository.findRange(instrumentId, timeframe, from, to);
        HistoricalData data;
        if (stored.isEmpty()) {
            data = marketDataProvider.historical(instrumentId, timeframe, from, to);
            candleJdbcRepository.upsertAll(instrumentId, timeframe, data.source(), data.candles());
        } else {
            data = new HistoricalData(instrumentId, timeframe, from, to, marketDataProvider.sourceName(), stored);
        }

        List<CandleResponse> candles = data.candles().stream()
                .map(c -> new CandleResponse(c.timestamp(), c.open(), c.high(), c.low(), c.close(), c.volume()))
                .toList();
        return new CandleSeriesResponse(
                instrumentId,
                timeframe.apiCode(),
                from,
                to,
                data.source(),
                candles
        );
    }

    public List<Candle> candles(long instrumentId, Timeframe timeframe, Instant from, Instant to) {
        instrumentService.require(instrumentId);
        List<Candle> stored = candleJdbcRepository.findRange(instrumentId, timeframe, from, to);
        if (!stored.isEmpty()) {
            return stored;
        }
        HistoricalData data = marketDataProvider.historical(instrumentId, timeframe, from, to);
        candleJdbcRepository.upsertAll(instrumentId, timeframe, data.source(), data.candles());
        return data.candles();
    }
}
