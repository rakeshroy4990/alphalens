package com.alphalens.service.workspace;

import com.alphalens.algo.ConfigurableRuleAlgorithm;
import com.alphalens.dto.response.PageResponse;
import com.alphalens.exception.ConflictException;
import com.alphalens.exception.InvalidRequestException;
import com.alphalens.exception.ResourceNotFoundException;
import com.alphalens.service.InstrumentService;
import com.alphalens.service.ResearchService;
import com.alphalens.service.score.StockScoreService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WorkspaceService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final InstrumentService instrumentService;
    private final ResearchService researchService;

    public WorkspaceService(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper,
            InstrumentService instrumentService,
            ResearchService researchService) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.instrumentService = instrumentService;
        this.researchService = researchService;
    }

    public Map<String, Object> createWatchlist(UUID userId, String name) {
        requireName(name);
        KeyHolder keys = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO watchlists (user_id, name) VALUES (?, ?) RETURNING id",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setObject(1, userId);
            ps.setString(2, name.trim());
            return ps;
        }, keys);
        long id = ((Number) keys.getKeys().get("id")).longValue();
        return getWatchlist(userId, id);
    }

    public List<Map<String, Object>> listWatchlists(UUID userId) {
        return jdbcTemplate.query(
                "SELECT id, name, created_at FROM watchlists WHERE user_id = ? ORDER BY id",
                (rs, i) -> {
                    long id = rs.getLong("id");
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", id);
                    row.put("name", rs.getString("name"));
                    row.put("createdAt", rs.getTimestamp("created_at").toInstant());
                    row.put("score", watchlistScore(id));
                    return row;
                },
                userId
        );
    }

    public Map<String, Object> getWatchlist(UUID userId, long watchlistId) {
        Map<String, Object> header = jdbcTemplate.query(
                "SELECT id, name, created_at FROM watchlists WHERE id = ? AND user_id = ?",
                rs -> {
                    if (!rs.next()) {
                        throw new ResourceNotFoundException("Watchlist not found");
                    }
                    return Map.<String, Object>of(
                            "id", rs.getLong("id"),
                            "name", rs.getString("name"),
                            "createdAt", rs.getTimestamp("created_at").toInstant()
                    );
                },
                watchlistId,
                userId
        );
        List<Map<String, Object>> items = jdbcTemplate.query(
                """
                        SELECT wi.id, wi.instrument_id, wi.sort_order
                        FROM watchlist_items wi
                        WHERE wi.watchlist_id = ?
                        ORDER BY wi.sort_order, wi.id
                        """,
                (rs, i) -> Map.of(
                        "id", rs.getLong("id"),
                        "instrumentId", rs.getLong("instrument_id"),
                        "sortOrder", rs.getInt("sort_order"),
                        "instrument", instrumentService.get(rs.getLong("instrument_id"))
                ),
                watchlistId
        );
        Map<String, Object> out = new LinkedHashMap<>(header);
        out.put("items", items);
        out.put("score", watchlistScore(watchlistId));
        return out;
    }

    public Map<String, Object> addWatchlistItem(UUID userId, long watchlistId, long instrumentId) {
        getWatchlist(userId, watchlistId);
        instrumentService.require(instrumentId);
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM watchlist_items WHERE watchlist_id = ? AND instrument_id = ?",
                Integer.class,
                watchlistId,
                instrumentId
        );
        if (exists != null && exists > 0) {
            throw new ConflictException("Instrument already on this watchlist");
        }
        Integer max = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(sort_order), -1) FROM watchlist_items WHERE watchlist_id = ?",
                Integer.class,
                watchlistId
        );
        jdbcTemplate.update(
                "INSERT INTO watchlist_items (watchlist_id, instrument_id, sort_order) VALUES (?, ?, ?)",
                watchlistId,
                instrumentId,
                max == null ? 0 : max + 1
        );
        return getWatchlist(userId, watchlistId);
    }

    public Map<String, Object> removeWatchlistItem(UUID userId, long watchlistId, long itemId) {
        getWatchlist(userId, watchlistId);
        int deleted = jdbcTemplate.update(
                "DELETE FROM watchlist_items WHERE id = ? AND watchlist_id = ?",
                itemId,
                watchlistId
        );
        if (deleted == 0) {
            throw new ResourceNotFoundException("Watchlist item not found");
        }
        return getWatchlist(userId, watchlistId);
    }

    public Map<String, Object> reorderWatchlist(UUID userId, long watchlistId, List<Long> itemIds) {
        getWatchlist(userId, watchlistId);
        for (int i = 0; i < itemIds.size(); i++) {
            jdbcTemplate.update(
                    "UPDATE watchlist_items SET sort_order = ? WHERE id = ? AND watchlist_id = ?",
                    i,
                    itemIds.get(i),
                    watchlistId
            );
        }
        return getWatchlist(userId, watchlistId);
    }

    public Map<String, Object> createAlert(UUID userId, long instrumentId, String type, Map<String, Object> definition) {
        instrumentService.require(instrumentId);
        if (type == null || type.isBlank()) {
            throw new InvalidRequestException("Alert type is required");
        }
        KeyHolder keys = new GeneratedKeyHolder();
        String json = writeJson(definition);
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO alerts (user_id, instrument_id, type, definition) VALUES (?, ?, ?, ?::jsonb) RETURNING id",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setObject(1, userId);
            ps.setLong(2, instrumentId);
            ps.setString(3, type.trim().toUpperCase());
            ps.setString(4, json);
            return ps;
        }, keys);
        long id = ((Number) keys.getKeys().get("id")).longValue();
        return getAlert(userId, id);
    }

    public List<Map<String, Object>> listAlerts(UUID userId) {
        return jdbcTemplate.query(
                "SELECT id FROM alerts WHERE user_id = ? ORDER BY id DESC",
                (rs, i) -> getAlert(userId, rs.getLong("id")),
                userId
        );
    }

    public Map<String, Object> getAlert(UUID userId, long alertId) {
        return jdbcTemplate.query(
                "SELECT id, instrument_id, type, definition, active, created_at FROM alerts WHERE id = ? AND user_id = ?",
                rs -> {
                    if (!rs.next()) {
                        throw new ResourceNotFoundException("Alert not found");
                    }
                    return Map.<String, Object>of(
                            "id", rs.getLong("id"),
                            "instrumentId", rs.getLong("instrument_id"),
                            "type", rs.getString("type"),
                            "definition", readJson(rs.getString("definition")),
                            "active", rs.getBoolean("active"),
                            "createdAt", rs.getTimestamp("created_at").toInstant()
                    );
                },
                alertId,
                userId
        );
    }

    public Map<String, Object> requestCoverage(UUID userId, Long instrumentId, String symbol) {
        if (instrumentId == null && (symbol == null || symbol.isBlank())) {
            throw new InvalidRequestException("instrumentId or symbol is required");
        }
        if (instrumentId != null) {
            instrumentService.require(instrumentId);
        }
        KeyHolder keys = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                            INSERT INTO coverage_requests (user_id, instrument_id, symbol, status)
                            VALUES (?, ?, ?, 'QUEUED') RETURNING id
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setObject(1, userId);
            if (instrumentId == null) {
                ps.setObject(2, null);
            } else {
                ps.setLong(2, instrumentId);
            }
            ps.setString(3, symbol);
            return ps;
        }, keys);
        long id = ((Number) keys.getKeys().get("id")).longValue();
        return coverage(id);
    }

    public List<Map<String, Object>> coverageQueue() {
        return jdbcTemplate.query(
                """
                        SELECT id FROM coverage_requests
                        ORDER BY CASE status WHEN 'QUEUED' THEN 0 WHEN 'IN_PROGRESS' THEN 1 ELSE 2 END, requested_at
                        """,
                (rs, i) -> coverage(rs.getLong("id"))
        );
    }

    public Map<String, Object> saveAlgorithm(UUID userId, String name, JsonNode definition) {
        parseAlgorithm(name, definition);
        KeyHolder keys = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO user_algorithms (user_id, name, definition) VALUES (?, ?, ?::jsonb) RETURNING id",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setObject(1, userId);
            ps.setString(2, name.trim());
            ps.setString(3, writeJson(definition));
            return ps;
        }, keys);
        long id = ((Number) keys.getKeys().get("id")).longValue();
        return getAlgorithm(userId, id);
    }

    public Map<String, Object> getAlgorithm(UUID userId, long algorithmId) {
        return jdbcTemplate.query(
                "SELECT id, name, definition, created_at, updated_at FROM user_algorithms WHERE id = ? AND user_id = ?",
                rs -> {
                    if (!rs.next()) {
                        throw new ResourceNotFoundException("Algorithm not found");
                    }
                    return Map.<String, Object>of(
                            "id", rs.getLong("id"),
                            "name", rs.getString("name"),
                            "definition", readJson(rs.getString("definition")),
                            "createdAt", rs.getTimestamp("created_at").toInstant(),
                            "updatedAt", rs.getTimestamp("updated_at").toInstant()
                    );
                },
                algorithmId,
                userId
        );
    }

    public List<Map<String, Object>> listAlgorithms(UUID userId) {
        return jdbcTemplate.query(
                "SELECT id FROM user_algorithms WHERE user_id = ? ORDER BY id DESC",
                (rs, i) -> getAlgorithm(userId, rs.getLong("id")),
                userId
        );
    }

    public Map<String, Object> runAlgorithm(UUID userId, long algorithmId, long instrumentId) {
        Map<String, Object> stored = getAlgorithm(userId, algorithmId);
        JsonNode definition = objectMapper.valueToTree(stored.get("definition"));
        ConfigurableRuleAlgorithm algorithm = parseAlgorithm((String) stored.get("name"), definition);
        return Map.of(
                "algorithmId", algorithmId,
                "instrumentId", instrumentId,
                "result", researchService.evaluate(instrumentId, algorithm)
        );
    }

    public Map<String, Object> duplicateAlgorithm(UUID userId, long algorithmId) {
        Map<String, Object> stored = getAlgorithm(userId, algorithmId);
        JsonNode definition = objectMapper.valueToTree(stored.get("definition"));
        return saveAlgorithm(userId, stored.get("name") + " copy", definition);
    }

    public void deleteAlgorithm(UUID userId, long algorithmId) {
        int deleted = jdbcTemplate.update("DELETE FROM user_algorithms WHERE id = ? AND user_id = ?", algorithmId, userId);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Algorithm not found");
        }
    }

    public PageResponse<Map<String, Object>> screen(Map<String, Object> filters, int page, int size) {
        var listed = instrumentService.list(0, 100);
        List<Map<String, Object>> matches = new ArrayList<>();
        for (var item : listed.items()) {
            var metrics = researchService.metricsForListedInstrument(item.instrumentId());
            var analytics = metrics.analytics();
            var valuation = metrics.valuation();
            var score = metrics.score();
            if (matchesFilter(filters, analytics.revenueCagr(), "revenueGrowth")
                    && matchesFilter(filters, analytics.epsCagr(), "epsGrowth")
                    && matchesFilter(filters, analytics.roce(), "roce")
                    && matchesFilter(filters, analytics.roe(), "roe")
                    && matchesFilter(filters, valuation.pe(), "pe")
                    && matchesFilter(filters, valuation.pb(), "pb")
                    && matchesFilter(filters, analytics.debtEquity(), "debt")
                    && matchesFilter(filters, analytics.promoterHolding(), "promoterHolding")
                    && matchesFilter(filters, score.overall(), "algoScore")) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("instrument", item);
                row.put("analytics", analytics);
                row.put("valuation", valuation);
                row.put("score", score);
                matches.add(row);
            }
        }
        int from = Math.min(page * size, matches.size());
        int to = Math.min(from + size, matches.size());
        return new PageResponse<>(
                matches.subList(from, to),
                page,
                size,
                matches.size(),
                size == 0 ? 0 : (int) Math.ceil(matches.size() / (double) size)
        );
    }

    public Map<String, Object> saveScreen(UUID userId, String name, Map<String, Object> filters) {
        requireName(name);
        KeyHolder keys = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO saved_screens (user_id, name, filters) VALUES (?, ?, ?::jsonb) RETURNING id",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setObject(1, userId);
            ps.setString(2, name.trim());
            ps.setString(3, writeJson(filters));
            return ps;
        }, keys);
        long id = ((Number) keys.getKeys().get("id")).longValue();
        return Map.of("id", id, "name", name.trim(), "filters", filters);
    }

    public List<Map<String, Object>> savedScreens(UUID userId) {
        return jdbcTemplate.query(
                "SELECT id, name, filters, created_at FROM saved_screens WHERE user_id = ? ORDER BY id DESC",
                (rs, i) -> Map.of(
                        "id", rs.getLong("id"),
                        "name", rs.getString("name"),
                        "filters", readJson(rs.getString("filters")),
                        "createdAt", rs.getTimestamp("created_at").toInstant()
                ),
                userId
        );
    }

    public ConfigurableRuleAlgorithm parseAlgorithm(String name, JsonNode definition) {
        if (definition == null || !definition.has("rules") || !definition.get("rules").isArray()) {
            throw new InvalidRequestException("Algorithm definition must be JSON with a rules array");
        }
        List<ConfigurableRuleAlgorithm.Rule> rules = new ArrayList<>();
        for (JsonNode rule : definition.get("rules")) {
            if (rule.has("code") || rule.has("script") || rule.has("javascript")) {
                throw new InvalidRequestException("Arbitrary code is not allowed in user algorithms");
            }
            rules.add(new ConfigurableRuleAlgorithm.Rule(
                    rule.path("metric").asText(),
                    ConfigurableRuleAlgorithm.Op.from(rule.path("op").asText()),
                    new BigDecimal(rule.path("value").asText())
            ));
        }
        return new ConfigurableRuleAlgorithm(name, rules);
    }

    private Map<String, Object> coverage(long id) {
        return jdbcTemplate.query(
                "SELECT id, user_id, instrument_id, symbol, status, requested_at FROM coverage_requests WHERE id = ?",
                rs -> {
                    if (!rs.next()) {
                        throw new ResourceNotFoundException("Coverage request not found");
                    }
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("userId", rs.getObject("user_id"));
                    row.put("instrumentId", rs.getObject("instrument_id"));
                    row.put("symbol", rs.getString("symbol"));
                    row.put("status", rs.getString("status"));
                    row.put("requestedAt", rs.getTimestamp("requested_at").toInstant());
                    return row;
                },
                id
        );
    }

    private BigDecimal watchlistScore(long watchlistId) {
        List<Long> ids = jdbcTemplate.query(
                "SELECT instrument_id FROM watchlist_items WHERE watchlist_id = ?",
                (rs, i) -> rs.getLong("instrument_id"),
                watchlistId
        );
        if (ids.isEmpty()) {
            return null;
        }
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (Long instrumentId : ids) {
            StockScoreService.ScoreBundle score = researchService.score(instrumentId);
            if (score.overall() != null) {
                sum = sum.add(score.overall());
                count++;
            }
        }
        return count == 0 ? null : sum.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP);
    }

    private boolean matchesFilter(Map<String, Object> filters, BigDecimal actual, String key) {
        if (filters == null || !filters.containsKey(key) || filters.get(key) == null) {
            return true;
        }
        if (actual == null) {
            return false;
        }
        Object raw = filters.get(key);
        if (raw instanceof Map<?, ?> range) {
            if (range.get("min") != null && actual.compareTo(new BigDecimal(range.get("min").toString())) < 0) {
                return false;
            }
            if (range.get("max") != null && actual.compareTo(new BigDecimal(range.get("max").toString())) > 0) {
                return false;
            }
            return true;
        }
        return actual.compareTo(new BigDecimal(raw.toString())) >= 0;
    }

    private void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("name is required");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (JsonProcessingException ex) {
            throw new InvalidRequestException("Invalid JSON payload");
        }
    }

    private Object readJson(String raw) {
        try {
            return objectMapper.readValue(raw, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            return Map.of();
        }
    }

    public Instant clock() {
        return Instant.now();
    }
}
