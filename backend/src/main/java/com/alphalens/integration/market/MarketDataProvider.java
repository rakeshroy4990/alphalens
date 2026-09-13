package com.alphalens.integration.market;

import com.alphalens.market.HistoricalData;
import com.alphalens.market.Quote;
import com.alphalens.market.Timeframe;

import java.time.Instant;

public interface MarketDataProvider {

    String sourceName();

    Quote quote(long instrumentId);

    HistoricalData historical(long instrumentId, Timeframe timeframe, Instant from, Instant to);
}
