package com.alphalens.service.workspace;

import com.alphalens.dto.response.InstrumentSummaryResponse;
import com.alphalens.dto.response.PageResponse;
import com.alphalens.service.InstrumentService;
import com.alphalens.service.ResearchService;
import com.alphalens.service.analytics.FundamentalAnalyticsService;
import com.alphalens.service.score.StockScoreService;
import com.alphalens.service.valuation.ValuationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkspaceServiceScreenTest {

    @Test
    void screenComputesBundledMetricsOncePerListedInstrument() {
        InstrumentService instrumentService = mock(InstrumentService.class);
        ResearchService researchService = mock(ResearchService.class);
        WorkspaceService workspaceService = new WorkspaceService(
                mock(JdbcTemplate.class),
                new ObjectMapper(),
                instrumentService,
                researchService
        );

        var item = new InstrumentSummaryResponse(
                1L, "INE000D00001", "DEMA", "590001", "Alpha", "Alpha", "DEMO", "DEMO", "NSE", "ACTIVE"
        );
        when(instrumentService.list(0, 100)).thenReturn(new PageResponse<>(List.of(item), 0, 100, 1, 1));
        when(researchService.metricsForListedInstrument(1L)).thenReturn(sampleMetrics());

        var page = workspaceService.screen(Map.of(), 0, 20);

        assertThat(page.items()).hasSize(1);
        assertThat(page.totalElements()).isEqualTo(1);
        verify(researchService).metricsForListedInstrument(1L);
        verify(researchService, never()).analytics(anyLong());
        verify(researchService, never()).valuation(anyLong());
        verify(researchService, never()).score(anyLong());
    }

    private static ResearchService.DerivedMetrics sampleMetrics() {
        var analytics = new FundamentalAnalyticsService.AnalyticsBundle(
                new BigDecimal("0.10"),
                new BigDecimal("0.10"),
                new BigDecimal("0.20"),
                new BigDecimal("0.12"),
                new BigDecimal("0.15"),
                new BigDecimal("0.16"),
                new BigDecimal("0.60"),
                new BigDecimal("0.30"),
                new BigDecimal("50"),
                BigDecimal.ZERO,
                List.of(),
                List.of(),
                List.of()
        );
        var valuation = new ValuationService.ValuationBundle(
                new BigDecimal("12"),
                new BigDecimal("2"),
                new BigDecimal("8"),
                new BigDecimal("1.0"),
                new BigDecimal("0.4"),
                new ValuationService.DcfResult(null, null, null, null)
        );
        var score = new StockScoreService.ScoreBundle(
                new BigDecimal("60.00"),
                Map.of("growth", new BigDecimal("40")),
                StockScoreService.DEFAULT_WEIGHTS
        );
        return new ResearchService.DerivedMetrics(analytics, valuation, score);
    }
}
