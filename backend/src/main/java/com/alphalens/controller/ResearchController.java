package com.alphalens.controller;

import com.alphalens.algo.AlgorithmResult;
import com.alphalens.dto.response.CandleSeriesResponse;
import com.alphalens.dto.response.IndicatorResponse;
import com.alphalens.dto.response.QuoteResponse;
import com.alphalens.dto.response.StockPageResponse;
import com.alphalens.fundamental.FinancialSnapshot;
import com.alphalens.service.CandleService;
import com.alphalens.service.MarketDataService;
import com.alphalens.service.ResearchService;
import com.alphalens.service.analytics.FundamentalAnalyticsService;
import com.alphalens.service.score.StockScoreService;
import com.alphalens.service.valuation.ValuationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/instruments/{instrumentId}")
public class ResearchController {

    private final MarketDataService marketDataService;
    private final CandleService candleService;
    private final ResearchService researchService;

    public ResearchController(
            MarketDataService marketDataService,
            CandleService candleService,
            ResearchService researchService) {
        this.marketDataService = marketDataService;
        this.candleService = candleService;
        this.researchService = researchService;
    }

    @GetMapping("/quote")
    public QuoteResponse quote(@PathVariable long instrumentId) {
        return marketDataService.quote(instrumentId);
    }

    @GetMapping("/candles")
    public CandleSeriesResponse candles(
            @PathVariable long instrumentId,
            @RequestParam String timeframe,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return candleService.series(instrumentId, timeframe, from, to);
    }

    @GetMapping("/indicators")
    public IndicatorResponse indicators(
            @PathVariable long instrumentId,
            @RequestParam(defaultValue = "1D") String timeframe,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return researchService.indicators(instrumentId, timeframe, from, to);
    }

    @GetMapping("/fundamentals")
    public FinancialSnapshot fundamentals(@PathVariable long instrumentId) {
        return researchService.fundamentals(instrumentId);
    }

    @GetMapping("/analytics")
    public FundamentalAnalyticsService.AnalyticsBundle analytics(@PathVariable long instrumentId) {
        return researchService.analytics(instrumentId);
    }

    @GetMapping("/valuation")
    public ValuationService.ValuationBundle valuation(@PathVariable long instrumentId) {
        return researchService.valuation(instrumentId);
    }

    @GetMapping("/score")
    public StockScoreService.ScoreBundle score(@PathVariable long instrumentId) {
        return researchService.score(instrumentId);
    }

    @GetMapping("/algorithm")
    public AlgorithmResult algorithm(@PathVariable long instrumentId) {
        return researchService.evaluateDefault(instrumentId);
    }

    @GetMapping("/page")
    public StockPageResponse page(@PathVariable long instrumentId) {
        return researchService.stockPage(instrumentId);
    }
}
