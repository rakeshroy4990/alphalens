package com.alphalens;

import com.alphalens.config.PostgresTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(PostgresTestContainerConfig.class)
class RlsLockdownIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void enablesRlsOnEveryPublicTableIncludingThoseCreatedAfterV3() {
        Integer v5 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '5' AND success = TRUE",
                Integer.class
        );
        assertThat(v5).isEqualTo(1);

        List<String> openTables = jdbcTemplate.queryForList(
                """
                        SELECT c.relname
                        FROM pg_class c
                        JOIN pg_namespace n ON n.oid = c.relnamespace
                        WHERE n.nspname = 'public'
                          AND c.relkind = 'r'
                          AND c.relname <> 'flyway_schema_history'
                          AND NOT c.relrowsecurity
                        ORDER BY c.relname
                        """,
                String.class
        );
        assertThat(openTables).isEmpty();
    }
}
