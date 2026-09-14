# AlphaLens

Indian stock intelligence platform. Research and analytics only — not investment advice.

Current slice: **Phases 0–26** on mock market data. **Phase 27** selected TrueData as the intended production vendor (license pending) — see [market-data-provider-decision.md](docs/market-data-provider-decision.md).

## Architecture

```text
Stock Information
    ↓
Stock Identification
    ↓
Market Data
    ↓
Financial Data
    ↓
Valuation
    ↓
Algorithms
    ↓
Portfolio
```

Runtime: Vue 3 frontend → Spring Boot REST API → PostgreSQL. Auth HTTP is `backend-auth-lib` (implement `AuthFacade` in the app). `MarketDataProvider` is MOCK first. Java still uses `Instrument` for stock identification.

Kite is a broker adapter. Tokens never go to Vue. OpenAI is optional and only explains backend-computed figures.

## Prerequisites

- Java 17+ (Java 21 is fine)
- Node.js 22+
- A Postgres URL in `backend/.env` (`SPRING_DATASOURCE_URL`). Cloud Supabase is the default; Docker Postgres is only used if that URL is localhost.
- If you use Colima for a localhost URL, `./scripts/start-dev.sh` starts it when the Docker daemon is down and points `DOCKER_HOST` at `unix://$HOME/.colima/default/docker.sock`

## Local development

```bash
./scripts/start-dev.sh
```

- UI: http://localhost:5173 (use `localhost`, not `127.0.0.1` — Google OAuth treats them as different origins)
- API: http://127.0.0.1:8088
- The browser must call `/api/...` on localhost (Vite proxies that to Spring). A Cloud Run URL in `frontend/.env` is for Firebase builds only; `./scripts/start-dev.sh` forces the local proxy.

Demo stocks: `DEMA`, `DEMB`, `DEMC`. Search from the home page. Stock URLs use `/stocks/{instrumentId}` (internal AlphaLens id).

Sign in from the header (login popup: email/password or Google if `VITE_GOOGLE_OAUTH_CLIENT_ID` and `APP_GOOGLE_OAUTH_WEB_CLIENT_ID` are set). Watchlists and portfolio open that popup when you are signed out. Tokens are httpOnly cookies — see [docs/api/auth.md](docs/api/auth.md).

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

Hosting serves the Vue `dist` only. `./scripts/deploy-firebase-hosting.sh` requires an absolute `VITE_API_BASE_URL` ending in `/api` (Cloud Run). Relative `/api` is only for local Vite and the Cloud Run nginx image.

## Docs

- [Architecture brief for LLM review](docs/architecture-review.md) — self-contained; paste into any model for a scored architecture review
- [Master development rule](docs/master-development-rule.md)
- [Research APIs](docs/api/research.md)
- [Database](docs/database.md)
- [Data licensing](docs/data-licensing.md)
- [Integrations registry](docs/integrations.md)
- [Market-data provider decision](docs/market-data-provider-decision.md)
