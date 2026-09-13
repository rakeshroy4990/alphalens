package com.alphalens.service;

import com.alphalens.algo.AlgorithmResult;
import com.alphalens.algo.ConfigurableRuleAlgorithm;
import com.alphalens.algo.StockContext;
import com.alphalens.dto.response.CandleSeriesResponse;
import com.alphalens.dto.response.IndicatorResponse;
import com.alphalens.dto.response.QuoteResponse;
import com.alphalens.dto.response.StockPageResponse;
import com.alphalens.fundamental.FinancialSnapshot;
import com.alphalens.fundamental.MockFundamentalCatalog;
import com.alphalens.integration.market.MarketDataProvider;
import com.alphalens.market.Candle;
import com.alphalens.market.Quote;
import com.alphalens.market.Timeframe;
import com.alphalens.service.analytics.FundamentalAnalyticsService;
import com.alphalens.service.indicator.IndicatorService;
import com.alphalens.service.score.StockScoreService;
import com.alphalens.service.valuation.ValuationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResearchService {

    private static final ConfigurableRuleAlgorithm DEFAULT_ALGO = new ConfigurableRuleAlgorithm(
            "Quality compounder",
            List.of(
                    new ConfigurableRuleAlgorithm.Rule("roe", ConfigurableRuleAlgorithm.Op.GTE, new BigDecimal("0.12")),
                    new ConfigurableRuleAlgorithm.Rule("roce", ConfigurableRuleAlgorithm.Op.GTE, new BigDecimal("0.12")),
                    new ConfigurableRuleAlgorithm.Rule("revenueCagr", ConfigurableRuleAlgorithm.Op.GTE, new BigDecimal("0.08")),
                    new ConfigurableRuleAlgorithm.Rule("debtEquity", ConfigurableRuleAlgorithm.Op.LTE, new BigDecimal("1.00"))
            )
    );

    private final InstrumentService instrumentService;
    private final MarketDataService marketDataService;
    private final CandleService candleService;
    private final MarketDataProvider marketDataProvider;
    private final MockFundamentalCatalog fundamentalCatalog;

    public ResearchService(
            InstrumentService instrumentService,
            MarketDataService marketDataService,
            CandleService candleService,
            MarketDataProvider marketDataProvider,
            MockFundamentalCatalog fundamentalCatalog) {
        this.instrumentService = instrumentService;
        this.marketDataService = marketDataService;
        this.candleService = candleService;
        this.marketDataProvider = marketDataProvider;
        this.fundamentalCatalog = fundamentalCatalog;
    }

    public StockPageResponse stockPage(long instrumentId) {
        var instrument = instrumentService.get(instrumentId);
        QuoteResponse quote = marketDataService.quote(instrumentId);
        FinancialSnapshot snapshot = fundamentalCatalog.snapshot(instrumentId);
        FundamentalAnalyticsService.AnalyticsBundle analytics = FundamentalAnalyticsService.analyze(snapshot);
        ValuationService.DcfAssumptions assumptions = defaultAssumptions(snapshot);
        Quote rawQuote = marketDataProvider.quote(instrumentId);
        ValuationService.ValuationBundle valuation = ValuationService.value(rawQuote, snapshot, assumptions);
        StockScoreService.ScoreBundle score = StockScoreService.score(analytics, valuation, quote.changePercent(), null);
        AlgorithmResult algo = DEFAULT_ALGO.evaluate(context(instrumentId, analytics, valuation));
        LocalDate to = LocalDate.now();
        CandleSeriesResponse candles = candleService.series(instrumentId, "1D", to.minusDays(180), to);
        return new StockPageResponse(
                instrument,
                quote,
                candles,
                score.overall(),
                score.components(),
                score.weights(),
                algo,
                snapshot,
                analytics,
                valuation,
                List.of("Research and education only. Not investment advice."),
                instrument.sectorName() == null ? List.of() : List.of("Sector: " + instrument.sectorName())
        );
    }

    public IndicatorResponse indicators(long instrumentId, String timeframe, LocalDate from, LocalDate to) {
        instrumentService.require(instrumentId);
        var series = candleService.series(instrumentId, timeframe, from, to);
        List<Candle> candles = series.candles().stream()
                .map(c -> new Candle(c.timestamp(), c.open(), c.high(), c.low(), c.close(), c.volume()))
                .toList();
        List<BigDecimal> closes = candles.stream().map(Candle::close).toList();
        var macd = IndicatorService.macd(closes, 12, 26, 9);
        var bands = IndicatorService.bollinger(closes, 20, new BigDecimal("2"));
        return new IndicatorResponse(
                instrumentId,
                series.timeframe(),
                series.source(),
                IndicatorService.sma(closes, 20),
                IndicatorService.ema(closes, 20),
                IndicatorService.rsi(closes, 14),
                macd.macd(),
                macd.signal(),
                macd.histogram(),
                IndicatorService.vwap(candles),
                bands.middle(),
                bands.upper(),
                bands.lower()
        );
    }

    public FinancialSnapshot fundamentals(long instrumentId) {
        instrumentService.require(instrumentId);
        return fundamentalCatalog.snapshot(instrumentId);
    }

    public FundamentalAnalyticsService.AnalyticsBundle analytics(long instrumentId) {
        return FundamentalAnalyticsService.analyze(fundamentals(instrumentId));
    }

    public ValuationService.ValuationBundle valuation(long instrumentId) {
        FinancialSnapshot snapshot = fundamentals(instrumentId);
        Quote quote = marketDataProvider.quote(instrumentId);
        return ValuationService.value(quote, snapshot, defaultAssumptions(snapshot));
    }

    public AlgorithmResult evaluateDefault(long instrumentId) {
        return DEFAULT_ALGO.evaluate(context(instrumentId, analytics(instrumentId), valuation(instrumentId)));
    }

    public AlgorithmResult evaluate(long instrumentId, ConfigurableRuleAlgorithm algorithm) {
        return algorithm.evaluate(context(instrumentId, analytics(instrumentId), valuation(instrumentId)));
    }

    public StockScoreService.ScoreBundle score(long instrumentId) {
        QuoteResponse quote = marketDataService.quote(instrumentId);
        return StockScoreService.score(analytics(instrumentId), valuation(instrumentId), quote.changePercent(), null);
    }

    public StockContext context(
            long instrumentId,
            FundamentalAnalyticsService.AnalyticsBundle analytics,
            ValuationService.ValuationBundle valuation) {
        Map<String, BigDecimal> metrics = new LinkedHashMap<>();
        metrics.put("revenueCagr", analytics.revenueCagr());
        metrics.put("epsCagr", analytics.epsCagr());
        metrics.put("roe", analytics.roe());
        metrics.put("roce", analytics.roce());
        metrics.put("debtEquity", analytics.debtEquity());
        metrics.put("pe", valuation.pe());
        metrics.put("pb", valuation.pb());
        metrics.put("promoterHolding", analytics.promoterHolding());
        return new StockContext(instrumentId, Map.copyOf(metrics));
    }

    private static ValuationService.DcfAssumptions defaultAssumptions(FinancialSnapshot snapshot) {
        FinancialSnapshot.Period last = snapshot.periods().get(snapshot.periods().size() - 1);
        BigDecimal fcf = last.fcf() == null ? BigDecimal.ZERO : last.fcf();
        BigDecimal netDebt = (last.debt() == null ? BigDecimal.ZERO : last.debt())
                .subtract(last.cash() == null ? BigDecimal.ZERO : last.cash());
        return new ValuationService.DcfAssumptions(
                new BigDecimal("0.0800"),
                new BigDecimal("0.0300"),
                new BigDecimal("0.1100"),
                fcf,
                new BigDecimal("10.0000"),
                netDebt
        );
    }

    public Instant now() {
        return Instant.now();
    }
}
