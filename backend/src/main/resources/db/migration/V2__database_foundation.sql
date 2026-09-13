-- Phase 2: relational foundation. Domain APIs are added in later phases.
-- Do not edit applied migrations; add a new version instead.

CREATE TABLE exchanges (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(16)  NOT NULL,
    name         VARCHAR(128) NOT NULL,
    country_code CHAR(2)      NOT NULL DEFAULT 'IN',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT exchanges_code_uq UNIQUE (code)
);

CREATE TABLE sectors (
    id         BIGSERIAL PRIMARY KEY,
    code       VARCHAR(64)  NOT NULL,
    name       VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT sectors_code_uq UNIQUE (code)
);

CREATE TABLE industries (
    id         BIGSERIAL PRIMARY KEY,
    sector_id  BIGINT       NOT NULL REFERENCES sectors (id),
    code       VARCHAR(64)  NOT NULL,
    name       VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT industries_code_uq UNIQUE (code)
);

CREATE INDEX industries_sector_id_idx ON industries (sector_id);

CREATE TABLE users (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email          VARCHAR(320) NOT NULL,
    password_hash  VARCHAR(255),
    auth_provider  VARCHAR(32)  NOT NULL DEFAULT 'EMAIL',
    status         VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT users_auth_provider_chk CHECK (auth_provider IN ('EMAIL', 'GOOGLE')),
    CONSTRAINT users_status_chk CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE UNIQUE INDEX users_email_lower_uq ON users (LOWER(email));

CREATE TABLE instruments (
    id            BIGSERIAL PRIMARY KEY,
    isin          VARCHAR(12)  NOT NULL,
    nse_symbol    VARCHAR(32),
    bse_symbol    VARCHAR(32),
    company_name  VARCHAR(256) NOT NULL,
    short_name    VARCHAR(128) NOT NULL,
    sector_id     BIGINT REFERENCES sectors (id),
    industry_id   BIGINT REFERENCES industries (id),
    exchange_id   BIGINT REFERENCES exchanges (id),
    status        VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT instruments_isin_uq UNIQUE (isin),
    CONSTRAINT instruments_status_chk CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELISTED'))
);

CREATE UNIQUE INDEX instruments_nse_symbol_uq
    ON instruments (nse_symbol)
    WHERE nse_symbol IS NOT NULL;

CREATE UNIQUE INDEX instruments_bse_symbol_uq
    ON instruments (bse_symbol)
    WHERE bse_symbol IS NOT NULL;

CREATE INDEX instruments_exchange_id_idx ON instruments (exchange_id);
CREATE INDEX instruments_sector_id_idx ON instruments (sector_id);
CREATE INDEX instruments_industry_id_idx ON instruments (industry_id);

INSERT INTO exchanges (code, name, country_code)
VALUES
    ('NSE', 'National Stock Exchange of India', 'IN'),
    ('BSE', 'BSE Limited', 'IN');

INSERT INTO app_bootstrap (id, phase)
VALUES (2, 'phase-2-database-foundation');
