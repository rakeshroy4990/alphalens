package com.alphalens.algo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigurableRuleAlgorithmTest {

    @Test
    void scoresShareOfPassedRulesAndDoesNotHardCodeAStock() {
        ConfigurableRuleAlgorithm algorithm = new ConfigurableRuleAlgorithm(
                "Quality",
                List.of(
                        new ConfigurableRuleAlgorithm.Rule("roe", ConfigurableRuleAlgorithm.Op.GTE, new BigDecimal("0.15")),
                        new ConfigurableRuleAlgorithm.Rule("pe", ConfigurableRuleAlgorithm.Op.LTE, new BigDecimal("25"))
                )
        );
        AlgorithmResult result = algorithm.evaluate(new StockContext(99L, Map.of(
                "roe", new BigDecimal("0.20"),
                "pe", new BigDecimal("40")
        )));
        assertThat(result.score()).isEqualByComparingTo("50.00");
        assertThat(result.passedRules()).containsExactly("roe GTE 0.15");
        assertThat(result.failedRules()).containsExactly("pe LTE 25");
        assertThat(result.name()).isEqualTo("Quality");
    }

    @Test
    void missingMetricFailsTheRuleInsteadOfInventingAValue() {
        ConfigurableRuleAlgorithm algorithm = new ConfigurableRuleAlgorithm(
                "ROE only",
                List.of(new ConfigurableRuleAlgorithm.Rule("roe", ConfigurableRuleAlgorithm.Op.GTE, new BigDecimal("0.10")))
        );
        AlgorithmResult result = algorithm.evaluate(new StockContext(1L, Map.of()));
        assertThat(result.failedRules()).hasSize(1);
        assertThat(result.score()).isEqualByComparingTo("0.00");
    }

    @Test
    void rejectsEmptyRuleSet() {
        assertThatThrownBy(() -> new ConfigurableRuleAlgorithm("Empty", List.of()))
                .hasMessageContaining("At least one rule");
    }
}
