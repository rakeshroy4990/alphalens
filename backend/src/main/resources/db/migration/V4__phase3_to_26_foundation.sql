-- Search indexes (Phase 3) and remaining product tables.
-- Demo instruments are fixtures for local/mock providers, not live market data.

CREATE INDEX instruments_company_name_lower_idx ON instruments (LOWER(company_name));
CREATE INDEX instruments_short_name_lower_idx ON instruments (LOWER(short_name));
CREATE INDEX instruments_nse_symbol_lower_idx ON instruments (LOWER(nse_symbol));
CREATE INDEX instruments_bse_symbol_lower_idx ON instruments (LOWER(bse_symbol));
CREATE INDEX instruments_isin_lower_idx ON instruments (LOWER(isin));

CREATE TABLE candles (
    instrument_id BIGINT        NOT NULL REFERENCES instruments (id),
    timeframe     VARCHAR(8)    NOT NULL,
    ts            TIMESTAMPTZ   NOT NULL,
    open          NUMERIC(18,4) NOT NULL,
    high          NUMERIC(18,4) NOT NULL,
    low           NUMERIC(18,4) NOT NULL,
    close         NUMERIC(18,4) NOT NULL,
    volume        BIGINT        NOT NULL,
    source        VARCHAR(32)   NOT NULL DEFAULT 'MOCK',
    PRIMARY KEY (instrument_id, timeframe, ts)
);

CREATE INDEX candles_ts_idx ON candles (ts);

DO $$
BEGIN
    CREATE EXTENSION IF NOT EXISTS timescaledb;
    PERFORM create_hypertable('candles', 'ts', if_not_exists => TRUE);
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'TimescaleDB unavailable; candles remains a regular table';
END $$;

CREATE TABLE financial_periods (
    id              BIGSERIAL PRIMARY KEY,
    instrument_id   BIGINT      NOT NULL REFERENCES instruments (id),
    period_type     VARCHAR(16) NOT NULL,
    period_end      DATE        NOT NULL,
    reported_date   DATE,
    currency        CHAR(3)     NOT NULL DEFAULT 'INR',
    unit            VARCHAR(16) NOT NULL DEFAULT 'CRORE',
    source          VARCHAR(64) NOT NULL DEFAULT 'MOCK',
    source_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT financial_periods_uq UNIQUE (instrument_id, period_type, period_end)
);

CREATE TABLE income_statements (
    period_id BIGINT PRIMARY KEY REFERENCES financial_periods (id) ON DELETE CASCADE,
    revenue   NUMERIC(20,4),
    ebitda    NUMERIC(20,4),
    ebit      NUMERIC(20,4),
    pat       NUMERIC(20,4),
    eps       NUMERIC(20,4)
);

CREATE TABLE balance_sheets (
    period_id     BIGINT PRIMARY KEY REFERENCES financial_periods (id) ON DELETE CASCADE,
    debt          NUMERIC(20,4),
    cash          NUMERIC(20,4),
    equity        NUMERIC(20,4),
    total_assets  NUMERIC(20,4)
);

CREATE TABLE cash_flow_statements (
    period_id BIGINT PRIMARY KEY REFERENCES financial_periods (id) ON DELETE CASCADE,
    fcf       NUMERIC(20,4)
);

CREATE TABLE shareholdings (
    period_id         BIGINT PRIMARY KEY REFERENCES financial_periods (id) ON DELETE CASCADE,
    promoter_holding  NUMERIC(8,4),
    promoter_pledge   NUMERIC(8,4)
);

CREATE TABLE dividends (
    id            BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT       NOT NULL REFERENCES instruments (id),
    ex_date       DATE         NOT NULL,
    amount        NUMERIC(12,4) NOT NULL,
    currency      CHAR(3)      NOT NULL DEFAULT 'INR'
);

CREATE TABLE valuation_assumptions (
    instrument_id      BIGINT PRIMARY KEY REFERENCES instruments (id),
    growth             NUMERIC(8,4) NOT NULL,
    terminal_growth    NUMERIC(8,4) NOT NULL,
    discount_rate      NUMERIC(8,4) NOT NULL,
    fcf                NUMERIC(20,4) NOT NULL,
    shares_outstanding NUMERIC(20,4) NOT NULL,
    net_debt           NUMERIC(20,4) NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE user_algorithms (
    id          BIGSERIAL PRIMARY KEY,
    user_id     UUID         REFERENCES users (id),
    name        VARCHAR(128) NOT NULL,
    definition  JSONB        NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE watchlists (
    id         BIGSERIAL PRIMARY KEY,
    user_id    UUID         REFERENCES users (id),
    name       VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE watchlist_items (
    id            BIGSERIAL PRIMARY KEY,
    watchlist_id  BIGINT NOT NULL REFERENCES watchlists (id) ON DELETE CASCADE,
    instrument_id BIGINT NOT NULL REFERENCES instruments (id),
    sort_order    INT    NOT NULL DEFAULT 0,
    CONSTRAINT watchlist_items_uq UNIQUE (watchlist_id, instrument_id)
);

CREATE TABLE alerts (
    id            BIGSERIAL PRIMARY KEY,
    user_id       UUID         REFERENCES users (id),
    instrument_id BIGINT       REFERENCES instruments (id),
    type          VARCHAR(32)  NOT NULL,
    definition    JSONB        NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE coverage_requests (
    id            BIGSERIAL PRIMARY KEY,
    user_id       UUID,
    instrument_id BIGINT REFERENCES instruments (id),
    symbol        VARCHAR(32),
    status        VARCHAR(32) NOT NULL DEFAULT 'QUEUED',
    requested_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE saved_screens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    UUID,
    name       VARCHAR(128) NOT NULL,
    filters    JSONB        NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE portfolio_holdings (
    id            BIGSERIAL PRIMARY KEY,
    user_id       UUID,
    instrument_id BIGINT REFERENCES instruments (id),
    quantity      NUMERIC(20,4) NOT NULL,
    average_price NUMERIC(18,4) NOT NULL,
    source        VARCHAR(32) NOT NULL DEFAULT 'MANUAL'
);

CREATE TABLE broker_connections (
    id            BIGSERIAL PRIMARY KEY,
    user_id       UUID,
    provider      VARCHAR(32) NOT NULL,
    consent       BOOLEAN NOT NULL DEFAULT FALSE,
    status        VARCHAR(32) NOT NULL DEFAULT 'DISCONNECTED',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE backtest_runs (
    id            BIGSERIAL PRIMARY KEY,
    algorithm_id  BIGINT REFERENCES user_algorithms (id),
    result        JSONB NOT NULL,
    simulated     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO sectors (code, name) VALUES ('DEMO', 'Demo / research fixtures')
ON CONFLICT (code) DO NOTHING;

INSERT INTO industries (sector_id, code, name)
SELECT s.id, 'DEMO_INDUSTRY', 'Demo industry'
FROM sectors s WHERE s.code = 'DEMO'
ON CONFLICT (code) DO NOTHING;

INSERT INTO instruments (isin, nse_symbol, bse_symbol, company_name, short_name, sector_id, industry_id, exchange_id, status)
SELECT
    v.isin, v.nse_symbol, v.bse_symbol, v.company_name, v.short_name,
    s.id, i.id, e.id, 'ACTIVE'
FROM (VALUES
    ('INE000D00001', 'DEMA', '590001', 'Alpha Test Industrials Limited', 'AlphaTest'),
    ('INE000D00002', 'DEMB', '590002', 'Beta Demo Energy Limited', 'BetaDemo'),
    ('INE000D00003', 'DEMC', '590003', 'Gamma Research Labs Limited', 'GammaLab')
) AS v(isin, nse_symbol, bse_symbol, company_name, short_name)
JOIN sectors s ON s.code = 'DEMO'
JOIN industries i ON i.code = 'DEMO_INDUSTRY'
JOIN exchanges e ON e.code = 'NSE'
ON CONFLICT (isin) DO NOTHING;

INSERT INTO users (id, email, password_hash, auth_provider, status)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'demo@alphalens.local',
    NULL,
    'EMAIL',
    'ACTIVE'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO app_bootstrap (id, phase)
VALUES (4, 'phase-3-to-26-product-foundation')
ON CONFLICT (id) DO NOTHING;
