package com.alphalens.service;

import com.alphalens.fundamental.MockFundamentalCatalog;
import com.alphalens.integration.market.MockMarketDataProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResearchServiceListedMetricsTest {

    private final InstrumentService instrumentService = mock(InstrumentService.class);
    private final ResearchService researchService = new ResearchService(
            instrumentService,
            mock(MarketDataService.class),
            mock(CandleService.class),
            new MockMarketDataProvider(),
            new MockFundamentalCatalog()
    );

    @Test
    void listedMetricsMatchPublicEndpointsWithoutReloadingInstrument() {
        when(instrumentService.require(1L)).thenReturn(null);

        var bundled = researchService.metricsForListedInstrument(1L);

        verify(instrumentService, never()).require(anyLong());
        assertThat(bundled.analytics()).isEqualTo(researchService.analytics(1L));
        assertThat(bundled.valuation()).isEqualTo(researchService.valuation(1L));
        assertThat(bundled.score()).isEqualTo(researchService.score(1L));
    }
}
