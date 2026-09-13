package com.alphalens.repository;

import com.alphalens.domain.Instrument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findByIsin(String isin);

    Optional<Instrument> findByNseSymbol(String nseSymbol);

    Optional<Instrument> findByBseSymbol(String bseSymbol);

    @Query("""
            SELECT i FROM Instrument i
            LEFT JOIN FETCH i.sector
            LEFT JOIN FETCH i.industry
            LEFT JOIN FETCH i.exchange
            WHERE i.id = :id
            """)
    Optional<Instrument> findDetailById(@Param("id") Long id);

    @Query("""
            SELECT i FROM Instrument i
            LEFT JOIN i.sector
            LEFT JOIN i.industry
            LEFT JOIN i.exchange
            WHERE LOWER(i.companyName) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(i.shortName) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(COALESCE(i.nseSymbol, '')) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(COALESCE(i.bseSymbol, '')) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(i.isin) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<Instrument> search(@Param("q") String q, Pageable pageable);
}
