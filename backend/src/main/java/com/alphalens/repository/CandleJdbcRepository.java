package com.alphalens.repository;

import com.alphalens.market.Candle;
import com.alphalens.market.Timeframe;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class CandleJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public CandleJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Candle> findRange(long instrumentId, Timeframe timeframe, Instant from, Instant to) {
        return jdbcTemplate.query(
                """
                        SELECT ts, open, high, low, close, volume
                        FROM candles
                        WHERE instrument_id = ?
                          AND timeframe = ?
                          AND ts >= ?
                          AND ts <= ?
                        ORDER BY ts
                        """,
                (rs, rowNum) -> new Candle(
                        rs.getTimestamp("ts").toInstant(),
                        rs.getBigDecimal("open"),
                        rs.getBigDecimal("high"),
                        rs.getBigDecimal("low"),
                        rs.getBigDecimal("close"),
                        rs.getLong("volume")
                ),
                instrumentId,
                timeframe.apiCode(),
                Timestamp.from(from),
                Timestamp.from(to)
        );
    }

    public void upsertAll(long instrumentId, Timeframe timeframe, String source, List<Candle> candles) {
        for (Candle candle : candles) {
            jdbcTemplate.update(
                    """
                            INSERT INTO candles (instrument_id, timeframe, ts, open, high, low, close, volume, source)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                            ON CONFLICT (instrument_id, timeframe, ts) DO UPDATE SET
                                open = EXCLUDED.open,
                                high = EXCLUDED.high,
                                low = EXCLUDED.low,
                                close = EXCLUDED.close,
                                volume = EXCLUDED.volume,
                                source = EXCLUDED.source
                            """,
                    instrumentId,
                    timeframe.apiCode(),
                    Timestamp.from(candle.timestamp()),
                    candle.open(),
                    candle.high(),
                    candle.low(),
                    candle.close(),
                    candle.volume(),
                    source
            );
        }
    }
}
