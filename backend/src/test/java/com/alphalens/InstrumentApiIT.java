package com.alphalens;

import com.alphalens.config.PostgresTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestContainerConfig.class)
class InstrumentApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void listsAndSearchesDemoInstrumentsWithoutUsingSymbolAsId() throws Exception {
        Integer v4 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '4' AND success = TRUE",
                Integer.class
        );
        assertThat(v4).isEqualTo(1);

        Long id = jdbcTemplate.queryForObject("SELECT id FROM instruments WHERE nse_symbol = 'DEMA'", Long.class);
        assertThat(id).isNotNull();

        mockMvc.perform(get("/api/instruments/search").param("q", "DEMA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].nseSymbol").value("DEMA"))
                .andExpect(jsonPath("$.items[0].instrumentId").value(id.intValue()));

        mockMvc.perform(get("/api/instruments/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isin").value("INE000D00001"))
                .andExpect(jsonPath("$.instrumentId").value(id.intValue()));

        mockMvc.perform(get("/api/instruments/" + id + "/quote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("MOCK"))
                .andExpect(jsonPath("$.lastPrice").exists());

        mockMvc.perform(get("/api/instruments/" + id + "/candles")
                        .param("timeframe", "1D")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("MOCK"))
                .andExpect(jsonPath("$.candles[0].open").exists())
                .andExpect(jsonPath("$.candles[0].close").exists());

        mockMvc.perform(get("/api/instruments/" + id + "/candles")
                        .param("timeframe", "99X")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-20"))
                .andExpect(status().isBadRequest());

        Integer stored = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM candles WHERE instrument_id = ?",
                Integer.class,
                id
        );
        assertThat(stored).isGreaterThan(0);
    }

    @Test
    void screenerReturnsBundledMetricsForListedInstruments() throws Exception {
        mockMvc.perform(post("/api/screener")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].instrument.instrumentId").exists())
                .andExpect(jsonPath("$.items[0].analytics.roce").exists())
                .andExpect(jsonPath("$.items[0].valuation.pe").exists())
                .andExpect(jsonPath("$.items[0].score.overall").exists())
                .andExpect(jsonPath("$.totalElements").value(3));
    }
}
