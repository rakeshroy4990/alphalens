package com.alphalens.dto.response;

import com.alphalens.algo.AlgorithmResult;
import com.alphalens.fundamental.FinancialSnapshot;
import com.alphalens.service.analytics.FundamentalAnalyticsService;
import com.alphalens.service.valuation.ValuationService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record StockPageResponse(
        InstrumentDetailResponse instrument,
        QuoteResponse quote,
        CandleSeriesResponse chart,
        BigDecimal overallScore,
        Map<String, BigDecimal> componentScores,
        Map<String, BigDecimal> scoreWeights,
        AlgorithmResult algorithm,
        FinancialSnapshot fundamentals,
        FundamentalAnalyticsService.AnalyticsBundle analytics,
        ValuationService.ValuationBundle valuation,
        List<String> riskNotes,
        List<String> news
) {
}
