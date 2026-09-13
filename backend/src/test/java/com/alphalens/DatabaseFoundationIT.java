package com.alphalens;

import com.alphalens.config.PostgresTestContainerConfig;
import com.alphalens.domain.AuthProvider;
import com.alphalens.domain.Exchange;
import com.alphalens.domain.Industry;
import com.alphalens.domain.Instrument;
import com.alphalens.domain.InstrumentStatus;
import com.alphalens.domain.Sector;
import com.alphalens.domain.UserAccount;
import com.alphalens.repository.ExchangeRepository;
import com.alphalens.repository.IndustryRepository;
import com.alphalens.repository.InstrumentRepository;
import com.alphalens.repository.SectorRepository;
import com.alphalens.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(PostgresTestContainerConfig.class)
class DatabaseFoundationIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private IndustryRepository industryRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void flywayCreatesFoundationTablesOnCleanDatabase() {
        Integer version = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version IN ('1', '2', '3') AND success = TRUE",
                Integer.class
        );
        assertThat(version).isEqualTo(3);

        List<String> tables = jdbcTemplate.queryForList(
                """
                        SELECT table_name
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name IN ('users', 'instruments', 'exchanges', 'sectors', 'industries')
                        ORDER BY table_name
                        """,
                String.class
        );
        assertThat(tables).containsExactly("exchanges", "industries", "instruments", "sectors", "users");

        Integer phase2 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_bootstrap WHERE phase = ?",
                Integer.class,
                "phase-2-database-foundation"
        );
        assertThat(phase2).isEqualTo(1);

        assertThat(exchangeRepository.findByCode("NSE")).isPresent();
        assertThat(exchangeRepository.findByCode("BSE")).isPresent();
    }

    @Test
    @Transactional
    void persistsCanonicalInstrumentAgainstLookupTables() {
        Exchange nse = exchangeRepository.findByCode("NSE").orElseThrow();
        Sector defence = sectorRepository.save(new Sector("DEFENCE", "Defence"));
        Industry aerospace = industryRepository.save(new Industry(defence, "AEROSPACE", "Aerospace & Defence"));

        Instrument saved = instrumentRepository.saveAndFlush(new Instrument(
                "INE000A00001",
                "TSTA",
                "500001",
                "Test Company Alpha Limited",
                "Alpha",
                defence,
                aerospace,
                nse
        ));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(InstrumentStatus.ACTIVE);
        assertThat(instrumentRepository.findByIsin("INE000A00001")).isPresent();
        assertThat(instrumentRepository.findByNseSymbol("TSTA")).isPresent();
    }

    @Test
    @Transactional
    void rejectsDuplicateIsin() {
        instrumentRepository.saveAndFlush(new Instrument(
                "INE000B00002",
                "TSTB",
                null,
                "Test Company Beta Limited",
                "Beta",
                null,
                null,
                null
        ));

        assertThatThrownBy(() -> instrumentRepository.saveAndFlush(new Instrument(
                "INE000B00002",
                "TSTB2",
                null,
                "Duplicate ISIN fixture",
                "Dup",
                null,
                null,
                null
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void rejectsDuplicateEmailIgnoringCase() {
        userAccountRepository.saveAndFlush(new UserAccount(
                "analyst@example.com",
                "hash",
                AuthProvider.EMAIL
        ));

        assertThatThrownBy(() -> userAccountRepository.saveAndFlush(new UserAccount(
                "Analyst@Example.com",
                null,
                AuthProvider.GOOGLE
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }
}
