package com.alphalens.service;

import com.alphalens.integration.ai.AIProvider;
import com.alphalens.service.analytics.FundamentalAnalyticsService;
import com.alphalens.service.score.StockScoreService;
import com.alphalens.service.valuation.ValuationService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AiExplanationService {

    private final ResearchService researchService;
    private final AIProvider aiProvider;

    public AiExplanationService(ResearchService researchService, AIProvider aiProvider) {
        this.researchService = researchService;
        this.aiProvider = aiProvider;
    }

    public Map<String, Object> explain(long instrumentId) {
        FundamentalAnalyticsService.AnalyticsBundle analytics = researchService.analytics(instrumentId);
        ValuationService.ValuationBundle valuation = researchService.valuation(instrumentId);
        StockScoreService.ScoreBundle score = researchService.score(instrumentId);
        Map<String, Object> structured = new LinkedHashMap<>();
        structured.put("instrumentId", instrumentId);
        structured.put("overallScore", score.overall());
        structured.put("componentScores", score.components());
        structured.put("revenueCagr", analytics.revenueCagr());
        structured.put("roe", analytics.roe());
        structured.put("roce", analytics.roce());
        structured.put("pe", valuation.pe());
        structured.put("dcfPerShare", valuation.dcf().valuePerShare());
        structured.put("dcfAssumptions", valuation.dcf().assumptions());
        structured.put("disclaimer", "AI must not calculate these figures; they are backend-computed.");
        return Map.of(
                "provider", aiProvider.name(),
                "structuredInputs", structured,
                "explanation", aiProvider.explain(structured)
        );
    }
}
