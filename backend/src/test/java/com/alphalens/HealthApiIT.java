package com.alphalens;

import com.alphalens.config.PostgresTestContainerConfig;
import com.alphalens.repository.HealthRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestContainerConfig.class)
class HealthApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HealthRepository healthRepository;

    @Test
    void healthEndpointReportsDatabaseUpAfterMigration() throws Exception {
        Integer bootstrapRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_bootstrap WHERE phase = ?",
                Integer.class,
                "phase-1-repository-foundation"
        );
        assertThat(bootstrapRows).isEqualTo(1);
        assertThat(healthRepository.isDatabaseReachable()).isTrue();

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("UP"))
                .andExpect(jsonPath("$.service").value("alphalens-backend"));
    }
}
