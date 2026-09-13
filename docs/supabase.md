# Supabase PostgreSQL

AlphaLens uses Supabase only as hosted Postgres. The Vue app does **not** use the Supabase JS client, `anon` key, or `service_role` key.

## `backend/.env`

```bash
SPRING_DATASOURCE_URL=postgresql://postgres.nbityfnkiavbzkkacaxm:{{password}}@aws-0-ap-northeast-1.pooler.supabase.com:6543/postgres
SPRING_DATASOURCE_PASSWORD=<database-password>
SUPABASE_URL=https://nbityfnkiavbzkkacaxm.supabase.co
SPRING_DATASOURCE_MAX_POOL_SIZE=5
```

`{{password}}` is replaced from `SPRING_DATASOURCE_PASSWORD`. The URI is rewritten to `jdbc:postgresql://...` and `sslmode=require` is added.

Port **6543** is the transaction pooler. The backend disables prepared statements and Flyway transactional locks for that port so JDBC and migrations work.

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
