package com.alphalens.integration.ai;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Explains precomputed structured research. Does not calculate PE, CAGR, ROCE, or DCF.
 */
@Component
public class MockAIProvider implements AIProvider {

    @Override
    public String name() {
        return "MOCK";
    }

    @Override
    public String explain(Map<String, Object> structuredInputs) {
        String facts = structuredInputs.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("; "));
        return "Educational summary of supplied research facts only (no new calculations): " + facts;
    }
}
