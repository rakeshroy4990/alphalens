package com.alphalens.service.portfolio;

import com.alphalens.exception.InvalidRequestException;
import com.alphalens.service.InstrumentService;
import com.alphalens.service.MarketDataService;
import com.alphalens.service.ResearchService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PortfolioService {

    private final JdbcTemplate jdbcTemplate;
    private final InstrumentService instrumentService;
    private final MarketDataService marketDataService;
    private final ResearchService researchService;

    public PortfolioService(
            JdbcTemplate jdbcTemplate,
            InstrumentService instrumentService,
            MarketDataService marketDataService,
            ResearchService researchService) {
        this.jdbcTemplate = jdbcTemplate;
        this.instrumentService = instrumentService;
        this.marketDataService = marketDataService;
        this.researchService = researchService;
    }

    public Map<String, Object> addManual(UUID userId, long instrumentId, BigDecimal quantity, BigDecimal averagePrice) {
        instrumentService.require(instrumentId);
        if (quantity == null || quantity.signum() <= 0 || averagePrice == null || averagePrice.signum() <= 0) {
            throw new InvalidRequestException("quantity and averagePrice must be positive");
        }
        jdbcTemplate.update(
                """
                        INSERT INTO portfolio_holdings (user_id, instrument_id, quantity, average_price, source)
                        VALUES (?, ?, ?, ?, 'MANUAL')
                        """,
                userId,
                instrumentId,
                quantity,
                averagePrice
        );
        return snapshot(userId);
    }

    public Map<String, Object> importCsv(UUID userId, String csv) {
        if (csv == null || csv.isBlank()) {
            throw new InvalidRequestException("CSV content is required");
        }
        try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
            String header = reader.readLine();
            if (header == null) {
                throw new InvalidRequestException("CSV is empty");
            }
            String line;
            int imported = 0;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 3) {
                    throw new InvalidRequestException("Each row needs instrumentId,quantity,averagePrice");
                }
                addManual(
                        userId,
                        Long.parseLong(parts[0].trim()),
                        new BigDecimal(parts[1].trim()),
                        new BigDecimal(parts[2].trim())
                );
                imported++;
            }
            Map<String, Object> snap = snapshot(userId);
            snap.put("importedRows", imported);
            return snap;
        } catch (InvalidRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidRequestException("Unable to parse holdings file. Use CSV: instrumentId,quantity,averagePrice");
        }
    }

    public Map<String, Object> snapshot(UUID userId) {
        List<Map<String, Object>> holdings = jdbcTemplate.query(
                "SELECT id, instrument_id, quantity, average_price, source FROM portfolio_holdings WHERE user_id = ? ORDER BY id",
                (rs, i) -> {
                    long instrumentId = rs.getLong("instrument_id");
                    var instrument = instrumentService.get(instrumentId);
                    var quote = marketDataService.quote(instrumentId);
                    var score = researchService.score(instrumentId);
                    BigDecimal qty = rs.getBigDecimal("quantity");
                    BigDecimal avg = rs.getBigDecimal("average_price");
                    BigDecimal marketValue = quote.lastPrice().multiply(qty);
                    BigDecimal cost = avg.multiply(qty);
                    BigDecimal pnl = marketValue.subtract(cost);
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("instrument", instrument);
                    row.put("quantity", qty);
                    row.put("averagePrice", avg);
                    row.put("lastPrice", quote.lastPrice());
                    row.put("marketValue", marketValue);
                    row.put("cost", cost);
                    row.put("pnl", pnl);
                    row.put("source", rs.getString("source"));
                    row.put("score", score.overall());
                    row.put("sector", instrument.sectorCode());
                    return row;
                },
                userId
        );
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal scoreSum = BigDecimal.ZERO;
        int scored = 0;
        Map<String, BigDecimal> sectorValue = new LinkedHashMap<>();
        for (Map<String, Object> holding : holdings) {
            BigDecimal mv = (BigDecimal) holding.get("marketValue");
            BigDecimal cost = (BigDecimal) holding.get("cost");
            totalValue = totalValue.add(mv);
            totalCost = totalCost.add(cost);
            if (holding.get("score") instanceof BigDecimal score) {
                scoreSum = scoreSum.add(score);
                scored++;
            }
            String sector = holding.get("sector") == null ? "UNKNOWN" : holding.get("sector").toString();
            sectorValue.merge(sector, mv, BigDecimal::add);
        }
        Map<String, BigDecimal> allocation = new LinkedHashMap<>();
        if (totalValue.signum() > 0) {
            for (var entry : sectorValue.entrySet()) {
                allocation.put(entry.getKey(), entry.getValue().divide(totalValue, 4, RoundingMode.HALF_UP));
            }
        }
        BigDecimal maxWeight = allocation.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("holdings", holdings);
        out.put("totalMarketValue", totalValue);
        out.put("totalCost", totalCost);
        out.put("pnl", totalValue.subtract(totalCost));
        out.put("portfolioScore", scored == 0 ? null : scoreSum.divide(BigDecimal.valueOf(scored), 2, RoundingMode.HALF_UP));
        out.put("sectorAllocation", allocation);
        out.put("concentration", maxWeight);
        out.put("riskNotes", List.of(
                "Manual portfolio. Not a broker snapshot.",
                "Concentration is the largest sector weight."
        ));
        return out;
    }
}
