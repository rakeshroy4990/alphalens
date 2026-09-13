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

Local Docker is skipped. Flyway applies V1–V3 on an empty project.

## Properties (all layers)

| Layer | Property |
| --- | --- |
| `backend/.env` | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_PASSWORD`, `SUPABASE_URL` |
| `application.properties` | `spring.datasource.*`, `app.supabase.url=${SUPABASE_URL:}` |
| Scripts | Load `.env`; skip Docker when the host is not localhost |
| Vue | Not used. Do not add `VITE_SUPABASE_*` keys |

## Security

Flyway **V3** enables RLS and revokes PostgREST `anon` / `authenticated` when those roles exist.

| Do | Do not |
| --- | --- |
| Keep the DB password in `backend/.env` | Put `anon` or `service_role` in Vue |
| Use a separate project for production | Commit `.env` |

## Health

```bash
curl http://127.0.0.1:8088/api/health
```

`database: UP` means the pooler accepted `SELECT 1`.
