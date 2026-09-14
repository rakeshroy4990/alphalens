# Database

Schema is owned by Flyway. Never change production tables by hand. `spring.jpa.hibernate.ddl-auto=none`.

## Migrations

| Version | File | Phase |
| --- | --- | --- |
| 1 | `V1__phase1_bootstrap.sql` | Connection probe (`app_bootstrap`) |
| 2 | `V2__database_foundation.sql` | `users`, `instruments`, `exchanges`, `sectors`, `industries` |
| 3 | `V3__supabase_rls_lockdown.sql` | RLS + revoke PostgREST grants (no-op-safe on local Docker) |
| 4 | `V4__phase3_to_26_foundation.sql` | Search indexes, candles, statements, workspace tables, demo fixtures |
| 5 | `V5__enable_rls_on_all_public_tables.sql` | RLS on public tables created after V3 (skips `flyway_schema_history`) |
| 6 | `V6__auth_refresh_tokens.sql` | `users.token_version`; hashed `refresh_tokens` |

## Tables (Phase 2)

### `exchanges`

Reference list of markets. Seeded: `NSE`, `BSE`.

### `sectors` / `industries`

Industry rows belong to a sector. Not seeded; populated when stock identification data is loaded.

### `users`

Application accounts. Email is unique case-insensitively. `password_hash` is nullable for Google-only accounts. `token_version` invalidates JWTs after a password change. Auth APIs: [api/auth.md](api/auth.md).

### `refresh_tokens`

Hashed refresh JWTs (`token_hash` is SHA-256 hex). Raw tokens are only in httpOnly cookies / JSON login responses, never stored in the clear.

### `instruments`

Stock identification table (Java entity `Instrument`). One reliable record per exchange-listed stock so market data, financial data, and scores attach to the right company. Primary key is internal `id` (`instrumentId`). Do not use NSE symbol as the identifier.

| Column | Notes |
| --- | --- |
| `id` | Internal AlphaLens stock id (`instrumentId`) |
| `isin` | Unique |
| `nse_symbol` / `bse_symbol` | Unique when present |
| `company_name` / `short_name` | Required |
| `sector_id` / `industry_id` / `exchange_id` | Optional until master data is complete |
| `status` | `ACTIVE`, `INACTIVE`, `DELISTED` |

## Local Docker

PostgreSQL from `infrastructure/docker-compose.yml` **only** when `SPRING_DATASOURCE_URL` points at localhost. If `.env` has a Supabase (or other remote) URL, local and Cloud Run both use that database. `./scripts/start-dev.sh` and `./scripts/start-backend.sh` start Colima first when the Docker daemon is down **and** the URL is local.

```bash
./scripts/start-backend.sh
```

A clean database applies V1–V6. An existing database applies only the missing versions.

Do not edit a migration after Cloud Run / Supabase has applied it. Flyway checksum mismatches fail startup.

If you edit a not-yet-shipped migration locally, recreate the Docker volume (local data only; not Supabase):

```bash
docker-compose -f infrastructure/docker-compose.yml down -v
```

Then run `./scripts/start-dev.sh` again. Flyway will apply V1–V6 on the empty database.

## Supabase

See [docs/supabase.md](supabase.md). Set `SPRING_DATASOURCE_*` in `backend/.env` to the Session pooler. Start scripts skip Docker whenever the URL is not localhost.

## Next

Licensed market-data and live Kite/OpenAI sessions, after [data-licensing.md](data-licensing.md) names a signed vendor. Phase 27 shortlist: [market-data-provider-decision.md](market-data-provider-decision.md).
