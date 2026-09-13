package com.alphalens.service.valuation;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValuationServiceTest {

    @Test
    void peIsNullWhenEpsIsNotPositive() {
        assertThat(ValuationService.pe(new BigDecimal("100"), BigDecimal.ZERO)).isNull();
        assertThat(ValuationService.pe(new BigDecimal("100"), new BigDecimal("-2"))).isNull();
    }

    @Test
    void peIsPriceOverEps() {
        assertThat(ValuationService.pe(new BigDecimal("100"), new BigDecimal("5")))
                .isEqualByComparingTo("20.000000");
    }

    @Test
    void dcfRejectsDiscountAtOrBelowTerminalGrowth() {
        var result = ValuationService.dcf(new ValuationService.DcfAssumptions(
                new BigDecimal("0.08"),
                new BigDecimal("0.05"),
                new BigDecimal("0.05"),
                new BigDecimal("100"),
                new BigDecimal("10"),
                BigDecimal.ZERO
        ));
        assertThat(result.valuePerShare()).isNull();
        assertThat(result.warning()).contains("discount rate");
    }

    @Test
    void dcfAssumptionsRemainVisibleOnSuccess() {
        var assumptions = new ValuationService.DcfAssumptions(
                new BigDecimal("0.08"),
                new BigDecimal("0.03"),
                new BigDecimal("0.11"),
                new BigDecimal("100"),
                new BigDecimal("10"),
                new BigDecimal("50")
        );
        var result = ValuationService.dcf(assumptions);
        assertThat(result.valuePerShare()).isNotNull();
        assertThat(result.assumptions()).isEqualTo(assumptions);
        assertThat(result.warning()).isNull();
    }

    @Test
    void percentileUsesInclusiveRank() {
        assertThat(ValuationService.percentile(new BigDecimal("20"), List.of(
                new BigDecimal("10"),
                new BigDecimal("20"),
                new BigDecimal("30")
        ))).isEqualByComparingTo("0.666667");
    }
}
