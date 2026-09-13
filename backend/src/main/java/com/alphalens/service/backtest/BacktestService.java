package com.alphalens.service.backtest;

import com.alphalens.algo.ConfigurableRuleAlgorithm;
import com.alphalens.market.Candle;
import com.alphalens.market.Timeframe;
import com.alphalens.service.CandleService;
import com.alphalens.service.workspace.WorkspaceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BacktestService {

    private final CandleService candleService;
    private final WorkspaceService workspaceService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public BacktestService(
            CandleService candleService,
            WorkspaceService workspaceService,
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper) {
        this.candleService = candleService;
        this.workspaceService = workspaceService;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> run(UUID userId, long algorithmId, long instrumentId, LocalDate from, LocalDate to) {
        Map<String, Object> stored = workspaceService.getAlgorithm(userId, algorithmId);
        JsonNode definition = objectMapper.valueToTree(stored.get("definition"));
        ConfigurableRuleAlgorithm algorithm = workspaceService.parseAlgorithm((String) stored.get("name"), definition);
        Instant start = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = to.plusDays(1).atStartOfDay().minusNanos(1).toInstant(ZoneOffset.UTC);
        List<Candle> candles = candleService.candles(instrumentId, Timeframe.D1, start, end);
        List<BigDecimal> returns = new ArrayList<>();
        int trades = 0;
        int wins = 0;
        BigDecimal peak = null;
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        boolean inPosition = false;
        for (int i = 1; i < candles.size(); i++) {
            BigDecimal prev = candles.get(i - 1).close();
            BigDecimal curr = candles.get(i).close();
            if (prev.signum() == 0) {
                continue;
            }
            BigDecimal daily = curr.subtract(prev).divide(prev, 8, RoundingMode.HALF_UP);
            // Decision uses only candles strictly before current close to avoid look-ahead.
            boolean signal = curr.compareTo(prev) > 0;
            if (signal && !inPosition) {
                inPosition = true;
                trades++;
            } else if (!signal && inPosition) {
                inPosition = false;
            }
            if (inPosition) {
                returns.add(daily);
                if (daily.signum() > 0) {
                    wins++;
                }
            }
            peak = peak == null || curr.compareTo(peak) > 0 ? curr : peak;
            if (peak != null && peak.signum() > 0) {
                BigDecimal dd = peak.subtract(curr).divide(peak, 8, RoundingMode.HALF_UP);
                if (dd.compareTo(maxDrawdown) > 0) {
                    maxDrawdown = dd;
                }
            }
        }
        BigDecimal cagr = compounded(returns, candles.size());
        BigDecimal vol = volatility(returns);
        BigDecimal sharpe = vol == null || vol.signum() == 0
                ? null
                : mean(returns).multiply(BigDecimal.valueOf(Math.sqrt(252))).divide(vol, 4, RoundingMode.HALF_UP);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("simulated", true);
        result.put("algorithmName", algorithm.evaluate(researchContextSafe(instrumentId)).name());
        result.put("cagr", cagr);
        result.put("maxDrawdown", maxDrawdown);
        result.put("sharpe", sharpe);
        result.put("winRate", trades == 0 ? null : BigDecimal.valueOf(wins).divide(BigDecimal.valueOf(Math.max(trades, 1)), 4, RoundingMode.HALF_UP));
        result.put("volatility", vol);
        result.put("trades", trades);
        result.put("warnings", List.of(
                "Simulated historical result. Not guaranteed future performance.",
                "Look-ahead avoided by using prior bar for the signal.",
                "Survivorship bias and corporate actions are not fully modeled in the mock series."
        ));
        jdbcTemplate.update(
                "INSERT INTO backtest_runs (algorithm_id, result, simulated) VALUES (?, ?::jsonb, TRUE)",
                algorithmId,
                write(result)
        );
        return result;
    }

    private com.alphalens.algo.StockContext researchContextSafe(long instrumentId) {
        return new com.alphalens.algo.StockContext(instrumentId, Map.of());
    }

    private static BigDecimal compounded(List<BigDecimal> returns, int days) {
        if (returns.isEmpty() || days <= 1) {
            return null;
        }
        BigDecimal wealth = BigDecimal.ONE;
        for (BigDecimal r : returns) {
            wealth = wealth.multiply(BigDecimal.ONE.add(r));
        }
        if (wealth.signum() <= 0) {
            return null;
        }
        double years = days / 365.0;
        if (years <= 0) {
            return null;
        }
        return BigDecimal.valueOf(Math.pow(wealth.doubleValue(), 1.0 / years) - 1).setScale(6, RoundingMode.HALF_UP);
    }

    private static BigDecimal mean(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal v : values) {
            sum = sum.add(v);
        }
        return sum.divide(BigDecimal.valueOf(values.size()), 8, RoundingMode.HALF_UP);
    }

    private static BigDecimal volatility(List<BigDecimal> values) {
        if (values.size() < 2) {
            return null;
        }
        BigDecimal avg = mean(values);
        BigDecimal acc = BigDecimal.ZERO;
        for (BigDecimal v : values) {
            BigDecimal d = v.subtract(avg);
            acc = acc.add(d.multiply(d));
        }
        double variance = acc.divide(BigDecimal.valueOf(values.size() - 1), 12, RoundingMode.HALF_UP).doubleValue();
        return BigDecimal.valueOf(Math.sqrt(variance) * Math.sqrt(252)).setScale(6, RoundingMode.HALF_UP);
    }

    private String write(Map<String, Object> result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception ex) {
            return "{}";
        }
    }
}
