# AlphaLens

Indian stock intelligence platform. Research and analytics only — not investment advice.

Current slice: **Phases 0–26** on mock market data. **Phase 27** selected TrueData as the intended production vendor (license pending) — see [market-data-provider-decision.md](docs/market-data-provider-decision.md).

## Architecture

```text
Vue 3 frontend  →  Spring Boot REST API  →  PostgreSQL
                       ↑
            MarketDataProvider (MOCK first)
```

Kite is a broker adapter. Tokens never go to Vue. OpenAI is optional and only explains backend-computed figures.

## Prerequisites

- Java 17+ (Java 21 is fine)
- Node.js 22+
- Docker (Docker Desktop or Colima) for local Postgres, **or** a Supabase project
- If you use Colima: `colima start` then `export DOCKER_HOST=unix://$HOME/.colima/default/docker.sock`

## Local development

```bash
./scripts/start-dev.sh
```

- UI: http://127.0.0.1:5173
- API: http://127.0.0.1:8088

Demo instruments: `DEMA`, `DEMB`, `DEMC`. Search from the home page. Stock URLs use `/stocks/{instrumentId}`.

## Tests

```bash
cd backend && ./gradlew test
cd frontend && npm test
```

## Google Cloud image

GCP project: [alphalens-508509](https://console.cloud.google.com/home/dashboard?project=alphalens-508509) (API / Cloud Run / Artifact Registry).  
Firebase Hosting is a separate project: `alphalens-a3cce`.

All-in-one container (Vue + API). Market data stays MOCK.

```bash
./scripts/gcp-use-project.sh
./scripts/gcp-build-image.sh
docker-compose -f infrastructure/docker-compose.cloud.yml up --build
```

Push and deploy: [docs/gcp-deploy.md](docs/gcp-deploy.md).

## Firebase Hosting (frontend)

Firebase project: [alphalens-a3cce](https://console.firebase.google.com/project/alphalens-a3cce/overview).

```bash
firebase login
./scripts/deploy-firebase-hosting.sh
```

Hosting URLs after deploy: `https://alphalens-a3cce.web.app` and `https://alphalens-a3cce.firebaseapp.com`.

Hosting serves the Vue `dist` only. Set `VITE_API_BASE_URL` to your public API before build — Firebase does not proxy `/api` to Spring Boot.

## Docs

- [Master development rule](docs/master-development-rule.md)
- [Research APIs](docs/api/research.md)
- [Database](docs/database.md)
- [Data licensing](docs/data-licensing.md)
- [Integrations registry](docs/integrations.md)
- [Market-data provider decision](docs/market-data-provider-decision.md)
