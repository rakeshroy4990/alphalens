-- Phase 1 bootstrap only. Domain tables are added in Phase 2.
CREATE TABLE app_bootstrap (
    id SMALLINT PRIMARY KEY,
    phase VARCHAR(64) NOT NULL,
    applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO app_bootstrap (id, phase)
VALUES (1, 'phase-1-repository-foundation');
