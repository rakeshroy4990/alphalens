# Database

Schema is owned by Flyway. Never change production tables by hand. `spring.jpa.hibernate.ddl-auto=none`.

## Migrations

| Version | File | Phase |
| --- | --- | --- |
| 1 | `V1__phase1_bootstrap.sql` | Connection probe (`app_bootstrap`) |
| 2 | `V2__database_foundation.sql` | `users`, `instruments`, `exchanges`, `sectors`, `industries` |
| 3 | `V3__supabase_rls_lockdown.sql` | RLS + revoke PostgREST grants (no-op-safe on local Docker) |
| 4 | `V4__phase3_to_26_foundation.sql` | Search indexes, candles, statements, workspace tables, demo fixtures |

## Tables (Phase 2)

### `exchanges`

Reference list of markets. Seeded: `NSE`, `BSE`.

### `sectors` / `industries`

Industry rows belong to a sector. Not seeded; populated when instrument master data is loaded.

### `users`

Application accounts. Email is unique case-insensitively. `password_hash` is nullable so Google OAuth users can be stored later. Auth APIs are not in Phase 2.

### `instruments`

Canonical security. Primary key is internal `id` (`instrumentId`). Do not use NSE symbol as the identifier.

| Column | Notes |
| --- | --- |
| `id` | Internal instrument id |
| `isin` | Unique |
| `nse_symbol` / `bse_symbol` | Unique when present |
| `company_name` / `short_name` | Required |
| `sector_id` / `industry_id` / `exchange_id` | Optional until master data is complete |
| `status` | `ACTIVE`, `INACTIVE`, `DELISTED` |

## Local Docker

PostgreSQL from `infrastructure/docker-compose.yml` when `SPRING_DATASOURCE_URL` points at localhost.

```bash
./scripts/start-backend.sh
```

A clean database applies V1–V4. An existing Phase 1 or 2 database applies only the missing versions.

## Supabase

See [docs/supabase.md](supabase.md). Set `SPRING_DATASOURCE_*` in `backend/.env` to the Session pooler. Start scripts skip Docker in that case.

## Next

Licensed market-data and live Kite/OpenAI sessions, after [data-licensing.md](data-licensing.md) names a signed vendor. Phase 27 shortlist: [market-data-provider-decision.md](market-data-provider-decision.md).
