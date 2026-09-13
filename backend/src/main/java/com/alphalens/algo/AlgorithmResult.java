package com.alphalens.algo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AlgorithmResult(
        String name,
        BigDecimal score,
        Map<String, BigDecimal> factors,
        List<String> passedRules,
        List<String> failedRules,
        Map<String, Object> explanationData
) {
}
