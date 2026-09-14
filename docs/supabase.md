# Supabase PostgreSQL

AlphaLens uses Supabase only as hosted Postgres. The Vue app does **not** use the Supabase JS client, `anon` key, or `service_role` key.

## `backend/.env`

```bash
SPRING_DATASOURCE_URL=postgresql://postgres:{{password}}@db.nbityfnkiavbzkkacaxm.supabase.co:5432/postgres
SPRING_DATASOURCE_PASSWORD=<database-password>
SUPABASE_URL=https://nbityfnkiavbzkkacaxm.supabase.co
SPRING_DATASOURCE_MAX_POOL_SIZE=5
```

Keep the env value as `postgresql://`. Do not convert it to `jdbc:` in `.env` or Cloud Run files. `{{password}}` is a placeholder only — the real password is read from `SPRING_DATASOURCE_PASSWORD` (local env or Secret Manager) and is not spliced into the URI.

If Cloud Run logs `password authentication failed for user "postgres"`, the secret does not match the current Supabase database password. Copy it from Project Settings → Database, then:

```bash
echo -n 'THE_REAL_DATABASE_PASSWORD' | gcloud secrets versions add SPRING_DATASOURCE_PASSWORD \
  --data-file=/dev/stdin \
  --project=alphalens-508509
```

On Cloud Run:

```bash
echo -n 'YOUR_DB_PASSWORD' | gcloud secrets versions add SPRING_DATASOURCE_PASSWORD \
  --data-file=/dev/stdin \
  --project=alphalens-508509

gcloud run deploy alphalens \
  --project=alphalens-508509 \
  --region=asia-south1 \
  --env-vars-file=infrastructure/gcp/cloud-run-env.yaml \
  --set-secrets=SPRING_DATASOURCE_PASSWORD=SPRING_DATASOURCE_PASSWORD:latest
```

Then:

```bash
./scripts/start-backend.sh
```

`SPRING_DATASOURCE_URL` is used as-is from `.env` for local `bootRun` and for Cloud Run. Local Docker Postgres starts only when that URL points at localhost. Flyway applies pending migrations on the configured database.

## Properties (all layers)

| Layer | Property |
| --- | --- |
| `backend/.env` | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_PASSWORD`, `SUPABASE_URL` |
| `application.properties` | `spring.datasource.*`, `app.supabase.url=${SUPABASE_URL:}` |
| Scripts | Load `.env`; skip Docker when the host is not localhost |
| Vue | Not used. Do not add `VITE_SUPABASE_*` keys |

## Security

Flyway **V5** enables RLS on `public` tables created after V3 and revokes PostgREST `anon` / `authenticated`. It does **not** `ALTER` `flyway_schema_history` (that deadlocks Flyway and hangs Cloud Run startup). Grants on that table are still revoked. The Vue app does not use the Data API. You can also turn off **Settings → API → Enable Data API** in the Supabase dashboard.

Do not edit V5 after the first successful Cloud Run apply — Flyway will fail checksum validation and the new revision will not start.

| Do | Do not |
| --- | --- |
| Keep the DB password in `backend/.env` | Put `anon` or `service_role` in Vue |
| Use a separate project for production | Commit `.env` |

## Health

```bash
curl http://127.0.0.1:8088/api/health
```

`database: UP` means the pooler accepted `SELECT 1`.
