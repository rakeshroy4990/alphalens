package com.alphalens.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "instruments")
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String isin;

    @Column(name = "nse_symbol", length = 32)
    private String nseSymbol;

    @Column(name = "bse_symbol", length = 32)
    private String bseSymbol;

    @Column(name = "company_name", nullable = false, length = 256)
    private String companyName;

    @Column(name = "short_name", nullable = false, length = 128)
    private String shortName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id")
    private Industry industry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_id")
    private Exchange exchange;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InstrumentStatus status = InstrumentStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Instrument() {
    }

    public Instrument(
            String isin,
            String nseSymbol,
            String bseSymbol,
            String companyName,
            String shortName,
            Sector sector,
            Industry industry,
            Exchange exchange) {
        this.isin = isin;
        this.nseSymbol = nseSymbol;
        this.bseSymbol = bseSymbol;
        this.companyName = companyName;
        this.shortName = shortName;
        this.sector = sector;
        this.industry = industry;
        this.exchange = exchange;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getIsin() {
        return isin;
    }

    public String getNseSymbol() {
        return nseSymbol;
    }

    public String getBseSymbol() {
        return bseSymbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getShortName() {
        return shortName;
    }

    public Sector getSector() {
        return sector;
    }

    public Industry getIndustry() {
        return industry;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public InstrumentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
