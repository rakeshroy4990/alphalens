package com.alphalens.service;

import com.alphalens.dto.response.QuoteResponse;
import com.alphalens.integration.market.MarketDataProvider;
import com.alphalens.market.Quote;
import org.springframework.stereotype.Service;

@Service
public class MarketDataService {

    private final InstrumentService instrumentService;
    private final MarketDataProvider marketDataProvider;

    public MarketDataService(InstrumentService instrumentService, MarketDataProvider marketDataProvider) {
        this.instrumentService = instrumentService;
        this.marketDataProvider = marketDataProvider;
    }

    public QuoteResponse quote(long instrumentId) {
        instrumentService.require(instrumentId);
        Quote quote = marketDataProvider.quote(instrumentId);
        return new QuoteResponse(
                quote.instrumentId(),
                quote.lastPrice(),
                quote.change(),
                quote.changePercent(),
                quote.open(),
                quote.high(),
                quote.low(),
                quote.previousClose(),
                quote.volume(),
                quote.asOf(),
                quote.source(),
                quote.market().name()
        );
    }
}
