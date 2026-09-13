package com.alphalens.algo;

import com.alphalens.exception.InvalidRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConfigurableRuleAlgorithm implements StockAlgorithm {

    private final String name;
    private final List<Rule> rules;

    public ConfigurableRuleAlgorithm(String name, List<Rule> rules) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("Algorithm name is required");
        }
        if (rules == null || rules.isEmpty()) {
            throw new InvalidRequestException("At least one rule is required");
        }
        this.name = name;
        this.rules = List.copyOf(rules);
    }

    @Override
    public AlgorithmResult evaluate(StockContext context) {
        List<String> passed = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        Map<String, BigDecimal> factors = new LinkedHashMap<>();
        int ok = 0;
        for (Rule rule : rules) {
            BigDecimal actual = context.metric(rule.metric());
            boolean pass = rule.matches(actual);
            String label = rule.metric() + " " + rule.op() + " " + rule.value();
            if (pass) {
                passed.add(label);
                ok++;
            } else {
                failed.add(label);
            }
            factors.put(rule.metric(), actual);
        }
        BigDecimal score = BigDecimal.valueOf(ok)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(rules.size()), 2, RoundingMode.HALF_UP);
        return new AlgorithmResult(
                name,
                score,
                factors,
                List.copyOf(passed),
                List.copyOf(failed),
                Map.of("ruleCount", rules.size(), "passedCount", ok)
        );
    }

    public record Rule(String metric, Op op, BigDecimal value) {
        boolean matches(BigDecimal actual) {
            if (actual == null) {
                return false;
            }
            int cmp = actual.compareTo(value);
            return switch (op) {
                case GTE -> cmp >= 0;
                case LTE -> cmp <= 0;
                case GT -> cmp > 0;
                case LT -> cmp < 0;
                case EQ -> cmp == 0;
            };
        }
    }

    public enum Op {
        GTE, LTE, GT, LT, EQ;

        public static Op from(String raw) {
            try {
                return Op.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            } catch (Exception ex) {
                throw new InvalidRequestException("Unsupported operator: " + raw);
            }
        }
    }
}
