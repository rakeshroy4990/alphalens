# AlphaLens — Architecture Brief for Independent LLM Review

**Purpose of this file:** Paste this document into any LLM (ChatGPT, Claude, Gemini, Cursor, etc.) and ask it to review the architecture. This file is self-contained. Do not require the model to read the repository.

**Audience:** Senior software architect / staff engineer / security reviewer acting as an independent critic.

**As-of date:** 2026-09-14  
**Product stage:** Phases 0–26 implemented on **MOCK** market data. Phase 27 (vendor selection) is documentation only. Phase 28 (live vendor adapter) is **blocked** until a signed commercial display/storage license is recorded.

---

## 0. Instructions for the reviewing LLM

You are an independent architecture reviewer. You have not built this product. Be direct. Do not praise process for its own sake. Distinguish:

1. **Intended architecture** (what the team says they will build)
2. **Implemented architecture** (what actually runs today)
3. **Fitness for the stated product** (Indian stock intelligence / Tickertape alternative)

### Rules for this review

- Rate the **current implemented system**, not the 48-phase roadmap, unless a roadmap item is already present in code.
- Penalize unimplemented claims that the docs present as if they were live (for example Redis, live Kite, live OpenAI, production auth).
- Reward explicit licensing discipline, port/adapter boundaries, deterministic finance, and documented non-goals.
- Do not recommend scraping NSE/BSE, Yahoo, or Kite as a market-data source. That is legally rejected.
- Do not recommend skipping auth, RLS, or typed APIs because “it is still early” if those surfaces are already user-facing.
- If something is intentionally mocked, say so. Do not treat mocks as production market data.
- Position the product as research/education, not investment advice. Flag any architecture that would present algo output as guaranteed advice.

### Required output format

Return **exactly** these sections, in this order:

1. **Executive verdict** — 5–8 sentences. Is the architecture sound for an Indian stock-research SaaS at this stage?
2. **Scorecard** — table with every dimension below, score 1–10, and one-line justification.
3. **Overall score** — weighted average using the weights below, rounded to one decimal. Then a letter grade: A 9.0+, B 8.0–8.9, C 7.0–7.9, D 6.0–6.9, F below 6.0.
4. **What is done well** — 5–8 bullets, specific.
5. **Architecture risks and smells** — numbered, severity `Critical | High | Medium | Low`, with a concrete fix.
6. **Constraint compliance** — pass/fail against the non-negotiables in section 12.
7. **Production readiness** — `Not production / Internal demo / Limited beta / Production` plus the top 5 blockers.
8. **Recommended next 90 days** — ordered list. Do not invent Phase 28 market-data work unless licensing is signed.
9. **Questions you would ask the team** — 5–10 questions.

Do not rewrite the product. Do not generate a new 48-phase plan.

### Scoring dimensions (weights sum to 100)

| Weight | Dimension | 10 means | 1 means |
| --- | --- | --- | --- |
| 12 | Domain model & identification | One `instrumentId` everywhere; ISIN/symbols are attributes | NSE symbol used as primary key |
| 12 | Data integrity & calculations | Deterministic, tested, null-safe, no silent interpolation | Fabricated or mixed-period math |
| 12 | Integration boundaries | All vendors behind ports; no Vue→vendor calls | Controllers/UI call third parties |
| 12 | Security & tenancy | Auth, CSRF/CORS, RLS, no spoofable identity | Open APIs + header-as-user |
| 10 | API design | Typed DTOs, validation, stable contracts | Untyped `Map` bodies, weak errors |
| 8 | Frontend layering | Views → stores → services; no secrets | Logic and HTTP in Vue templates |
| 8 | Data architecture | Schema ownership, migrations, time-series plan | Ad-hoc tables, ddl-auto |
| 8 | Observability & ops | Health, deploy, secrets, rate limits that scale | In-memory only, no health |
| 8 | Regulatory & licensing | Display/storage rights treated as a hard gate | Live quotes without a license |
| 5 | Test strategy | Unit + IT covering finance and APIs | Untested money math |
| 5 | Incremental delivery | Honest mocks vs live; phase gates | Fake completeness |

---

## 1. Product

**AlphaLens** is a production-intent Indian stock intelligence platform (Tickertape alternative). It is **research, analytics, screening, portfolio analytics, and education** — not personalized investment advice and not a broker.

Differentiation target:

```text
DATA + ANALYTICS + ALGORITHMS + EXPLANATION
```

The product should eventually answer:

- Why is this stock attractive or unattractive?
- What changed?

**Hard product rule:** LLMs must never calculate PE, CAGR, ROCE, DCF, returns, or allocation. Java computes those. AI may only explain structured results the backend already computed.

Product language (docs and UI). Java still uses `Instrument` / `instrumentId` as the technical record:

```text
Stock Information
  → Stock Identification
  → Market Data
  → Financial Data
  → Valuation
  → Algorithms
  → Portfolio
```

Kite (Zerodha) is a **broker adapter only**. It is not the market-data architecture. Redistributing Kite market data on an external product violates exchange data-vending policy.

Initial universe target (later): 100–300 listed names. Today: three demo fixtures (`DEMA`, `DEMB`, `DEMC`).

---

## 2. Current runtime vs target runtime

### What runs today

```text
Browser (Vue 3 SPA)
    │  HTTP JSON (camelCase)
    │  never calls a market-data vendor
    ▼
Spring Boot 3.4 REST API  (Java 17)
    │  Controller → Service → Repository / port
    ▼
PostgreSQL 16  (local Docker or Supabase)
    candles table (Timescale hypertable IF extension exists; else plain table)
```

Market quotes, candles, and fundamentals are generated by **`MockMarketDataProvider` / `MockFundamentalCatalog`** and labeled `MOCK`.

Identity is a **demo user**. Optional header `X-User-Id`; if missing/invalid, UUID `00000000-0000-0000-0000-000000000001` (`demo@alphalens.local`).

OpenAI is **`MockAIProvider`**. Kite is a **stub** (`KiteBrokerProvider`) that does not place live orders. Redis is **not used**. News, billing, S3, and real-time WebSockets are **not implemented**.

### Target architecture (not fully built)

```text
DATA SOURCES (licensed vendor, fundamentals, news)
        → INGESTION → NORMALIZATION
        → PostgreSQL + TimescaleDB → REDIS
        → DOMAIN SERVICES (Chart / Algo / Portfolio)
        → REST API → Vue research UI
        → later: Kite broker adapter (tokens server-side, encrypted)
```

### Honest status

| Capability | Status |
| --- | --- |
| Stock identification (internal id, ISIN, NSE/BSE symbols) | Live, 3 demo names |
| Search + pagination | Live |
| Quotes / candles / indicators | Live on MOCK series |
| Fundamentals, CAGR/margins, PE/PB/DCF, scores, default algo | Live on MOCK catalog |
| Screener, watchlists, alerts, coverage queue, user JSON algos | Live APIs + DB tables; identity is demo |
| Manual/CSV portfolio | Live against demo user |
| AI explanation | Live path; mock explainer of precomputed facts |
| Broker login/sync | Stub; no live session |
| Order preview/confirm | In-memory preview; confirm returns `SIMULATED_REJECTED` |
| Backtests | Simulated JSON; labeled simulated |
| Production market-data adapter | **Forbidden until license is signed** |
| Email/password + Google OAuth | **Not built** |
| Redis cache | **Not built** |
| Real-time streaming | **Not built** (Phase 32+) |

---

## 3. Stack (do not treat as interchangeable)

| Layer | Choice | Notes |
| --- | --- | --- |
| Frontend | Vue 3.5, TypeScript, Vite 8, Pinia, Vue Router, Tailwind CSS 4 | TradingView Lightweight Charts 4.2.3 for rendering only |
| HTTP client | Axios in `services/` only | No raw Axios in Vue views |
| Backend | Java 17, Spring Boot 3.4.2, Gradle, Spring Security, Validation, JPA | `ddl-auto=none` |
| Migrations | Flyway V1–V5 | Checksums are immutable after Cloud Run / Supabase apply |
| Database | PostgreSQL 16 | Timescale optional; Supabase used as hosted Postgres only |
| Cache | Redis (planned) | Not required for the mock slice |
| Auth (planned) | Email/password + Google OAuth | Not implemented; demo header today |
| AI (planned) | OpenAI via backend only | Mock today; never put keys in Vue |
| Broker (planned) | Zerodha Kite Connect | Stub; `api_secret` / `access_token` must stay server-side |
| Object storage | S3-compatible (planned) | Not implemented |
| Deploy | Cloud Run all-in-one image (nginx + Java) + Firebase Hosting for SPA | GCP `alphalens-508509`, Firebase `alphalens-a3cce` |
| Local | `./scripts/start-dev.sh` — UI `:5173`, API `:8088`, Vite proxies `/api` | Postgres from `SPRING_DATASOURCE_URL` (Supabase when set; Docker only for localhost) |

---

## 4. Repository layout

```text
alphalens/
├── frontend/          Vue SPA
├── backend/           Spring Boot
├── docs/              architecture, APIs, licensing, deploy
├── infrastructure/    local compose + GCP Dockerfile/nginx
└── scripts/           start-dev, Cloud Build helpers, Firebase deploy
```

Backend packages in use: `controller`, `service`, `repository`, `domain`, `dto`, `mapper`, `config`, `exception`, `integration`, plus focused packages `market`, `algo`, `fundamental`, `identity`.

Frontend: `views`, `stores`, `services`, `components` (system + primitives), `layouts`, `router`, `types`, `composables`, `configs`.

---

## 5. Domain model

### Stock identification (`instruments`)

Never use NSE symbol as the primary identifier. Everything attaches to internal `instrumentId`.

```text
Instrument
  id (PK, instrumentId)
  isin                  unique
  nseSymbol / bseSymbol unique when present
  companyName / shortName
  sector / industry / exchange (FKs)
  status                ACTIVE | INACTIVE | DELISTED
```

The same company may have NSE and BSE listings, symbol changes, and separate price vs financial series. Identification is the join key for all of that.

### Market data

```text
Quote     instrumentId, lastPrice, change, OHLC, previousClose, volume, asOf, source, market
Candle    timestamp, open, high, low, close, volume
Timeframe 1M, 5M, 15M, 1H, 1D, 1W
HistoricalData  instrumentId + timeframe + candles + source
```

Persisted `candles` include `source` (default `MOCK`). Product UI timeframes mentioned in the master rule (`1D 1W 1M 6M 1Y 5Y MAX`) are range presets; the API timeframe enum above is the bar size.

### Financial data

Normalized statements keyed by `financial_periods` (`period_type`, `period_end`, `reported_date`, `currency`, `unit`, `source`, `source_timestamp`). Child tables: income, balance sheet, cash flow, shareholding. Dividends are a separate series.

**Implementation note:** research math currently reads `MockFundamentalCatalog` in process, not necessarily rows from `financial_periods`. The tables exist for the later licensed catalog.

Integrity rules the team claims:

- No fabricated live prices
- No silent interpolation of missing financials
- No look-ahead
- Do not mix quarterly and annual incorrectly
- Do not destroy raw series when adjusting for corporate actions (Phase 31 — not built)
- Nulls, zero denominators, negatives must not become silent pretty numbers

### Valuation and scores

Valuation: PE, PB, EV/EBITDA, PEG, DCF. DCF assumptions (growth, terminal growth, discount rate, FCF, shares, net debt) must be visible to the user.

Stock score 0–100 with **component scores never hidden**. Default weights:

| Component | Weight |
| --- | --- |
| Growth | 0.20 |
| Quality | 0.20 |
| Financial | 0.15 |
| Valuation | 0.20 |
| Momentum | 0.15 |
| Risk | 0.10 |

Algo engine: configuration-driven rules (`metric`, `op`, `threshold`), not hard-coded stock picks. Result: `score`, `factors`, `passedRules`, `failedRules`, `explanationData`. User algorithms are JSON/DSL only — **no user-supplied code execution**.

Default stock-page algorithm is currently constructed in `ResearchService` (ROE/ROCE/revenue CAGR/debt-equity thresholds), not loaded from `user_algorithms`.

---

## 6. Backend architecture

### Layering

```text
HTTP controllers (thin)
    → domain services (Research, Instrument, Candle, Workspace, Portfolio, Trading, Backtest, AI)
        → ports: MarketDataProvider, AIProvider, BrokerProvider, NotificationPort
        → JPA repositories (identification) or JdbcTemplate (workspace/candles)
            → PostgreSQL
```

Controllers must not call vendors. Vue must not call vendors.

### Ports (implemented)

```text
MarketDataProvider
  sourceName()
  quote(instrumentId)
  historical(instrumentId, timeframe, from, to)
  └── MockMarketDataProvider          ACTIVE
  └── TrueData adapter                NOT ALLOWED YET

AIProvider
  name()
  explain(structuredInputs)           # must not compute metrics
  └── MockAIProvider                  ACTIVE

BrokerProvider
  loginUrl, status, holdings, positions, orders, trades
  └── KiteBrokerProvider              STUB (no live HTTP)

NotificationPort
  └── LoggingNotificationPort         ACTIVE (logs only)
```

`NewsProvider` and `BillingProvider` are specified in rules and **do not exist in code**.

`ResearchService` depends on the concrete `MockFundamentalCatalog`, not a `FundamentalProvider` port.

### Notable services

| Service | Responsibility |
| --- | --- |
| `InstrumentService` | List, search, get; never key by NSE symbol |
| `MarketDataService` | Quote DTOs via `MarketDataProvider` |
| `CandleService` | Range queries; max bars enforced |
| `IndicatorService` | SMA, EMA, RSI, MACD, VWAP, Bollinger (deterministic) |
| `FundamentalAnalyticsService` | CAGRs, margins, trends |
| `ValuationService` | Multiples + DCF with assumptions |
| `StockScoreService` | Weighted 0–100 |
| `WorkspaceService` | Watchlists, alerts, coverage, user algos, screener, saved screens |
| `PortfolioService` | Manual + CSV holdings |
| `TradingService` | Preview then confirm; never auto-trade from algo signals |
| `BacktestService` | Simulated runs, labeled as such |
| `AiExplanationService` | Packs backend numbers → `AIProvider` |

`ValuationService`, `StockScoreService`, `IndicatorService`, and `FundamentalAnalyticsService` are largely **static utility classes**, not Spring beans.

### API surface

JSON is camelCase. Paths still say `/api/instruments`.

| Method | Path | Notes |
| --- | --- | --- |
| GET | `/api/health` | Public; 200 if Postgres `SELECT 1`, else 503 |
| GET | `/api/instruments?page&size` | Paginated identification |
| GET | `/api/instruments/search?q=` | Name, symbol, ISIN |
| GET | `/api/instruments/{id}` | Detail |
| GET | `/api/instruments/{id}/quote` | MOCK quote |
| GET | `/api/instruments/{id}/candles` | OHLCV |
| GET | `/api/instruments/{id}/indicators` | Six indicators |
| GET | `/api/instruments/{id}/fundamentals` | Statements snapshot |
| GET | `/api/instruments/{id}/analytics` | Derived metrics |
| GET | `/api/instruments/{id}/valuation` | Multiples + DCF |
| GET | `/api/instruments/{id}/score` | Components + weights |
| GET | `/api/instruments/{id}/algorithm` | Default rules |
| GET | `/api/instruments/{id}/page` | Aggregate stock page |
| GET | `/api/research/{id}/explain` | AI explains structured inputs |
| POST | `/api/screener` | Filters; computes listed metrics once per name |
| CRUD-ish | `/api/watchlists`, `/alerts`, `/coverage-requests`, `/user-algorithms`, `/saved-screens` | Demo user |
| GET/POST | `/api/portfolio` | Manual / CSV |
| GET/POST | `/api/broker/*` | Kite stub |
| POST | `/api/orders/preview` + `/confirm` | Confirmation required |
| POST | `/api/backtests` | Simulated |

Research identification/quote/candle/page responses use typed records. Workspace, portfolio, broker, trading, backtest, and explain endpoints often return `Map<String, Object>` and accept loosely typed JSON maps.

### Error handling

Shared `GlobalExceptionHandler` with body:

```json
{ "status": 400, "error": "Bad Request", "message": "...", "details": [], "timestamp": "..." }
```

Validation exists on instrument list/search paging (`@Min` / `@Max`). Several POST bodies are unvalidated maps.

---

## 7. Frontend architecture

```text
Route (Vue Router)
  → App.vue → AppLayout (header, body, footer)
    → View
      → Pinia store (state + orchestration)
        → service module (only HTTP boundary, Axios)
          → /api on same origin (Vite proxy or nginx, or absolute Cloud Run URL on Firebase)
```

Routes: `/home`, `/stocks/:instrumentId`, `/screener`, `/watchlists`, `/portfolio`.

Principles the team states:

- No business logic in Vue templates
- Services are the only API boundary
- Shared chrome is config (`siteChrome`)
- No secrets, no vendor sockets, no Supabase anon/service_role keys in Vue
- Do not show “System status” on research pages
- UI copy says “stock”, never “instrument”
- Stock page priority: Price → Chart → Score → Why → Fundamentals → Valuation → Risks → News

Chart data comes only from backend candles. Lightweight Charts is Apache 2.0; keep TradingView attribution.

Frontend does **not** implement login. It does not send `X-User-Id` today, so all workspace writes collapse to the demo user.

---

## 8. Data architecture

Schema is owned by Flyway. Never change production tables by hand.

| Version | Role |
| --- | --- |
| V1 | Bootstrap probe |
| V2 | `users`, `instruments`, `exchanges`, `sectors`, `industries` |
| V3 | Early RLS + revoke PostgREST grants (no-op-safe on local Docker) |
| V4 | Search indexes, candles, financials, workspace, demo fixtures |
| V5 | RLS on public tables created after V3; skips `ALTER` on `flyway_schema_history` (that deadlocks Flyway) |

### Core tables

Reference: `exchanges`, `sectors`, `industries`  
Identification: `instruments`  
Accounts: `users` (`email` unique case-insensitively; `password_hash` nullable for future OAuth)  
Market: `candles` (`PRIMARY KEY (instrument_id, timeframe, ts)`)  
Fundamentals: `financial_periods` + statement children, `dividends`, `valuation_assumptions`  
Workspace: `watchlists`, `watchlist_items`, `alerts`, `coverage_requests`, `saved_screens`, `user_algorithms`  
Portfolio: `portfolio_holdings`  
Broker: `broker_connections`  
Research ops: `backtest_runs` (`simulated` default true)

Timescale: V4 tries `create_hypertable('candles', 'ts')` and **swallows failure** so local/Supabase Postgres still boots.

Supabase is **Postgres hosting only**. Vue must not use the Data API. V3/V5 revoke `anon` / `authenticated` grants. App connects as the database role via Session/transaction pooler. Port 6543 disables prepared statements automatically.

---

## 9. Security (as implemented)

```text
Spring Security
  CSRF disabled
  CORS allowlist: localhost, 127.0.0.1, *.web.app, *.firebaseapp.com, *.run.app
  Session: STATELESS
  /api/** permitAll
  everything else authenticated (there is no other useful surface)

RateLimitFilter
  120 requests / 60s / remoteAddr
  in-process ConcurrentHashMap  → not shared across Cloud Run instances

Identity
  DemoUsers.resolve(X-User-Id) → UUID or demo UUID
  Spoofable; no signature, no session, no password login

Trading confirmations
  stored in TradingService ConcurrentHashMap
  lost on restart; not sticky across instances

Secrets
  DB password via env / Secret Manager
  {{password}} placeholder in JDBC URL replaced at runtime
  Kite/OpenAI keys not in Vue
  .env gitignored
```

This is appropriate only while the product is a **research demo on mock data**. It is not a multi-tenant production security model.

---

## 10. Deployment

Local:

```text
Vite :5173  --proxy /api-->  Spring :8088  -->  Docker Postgres :5432
                                         \->  or Supabase session pooler
```

Cloud Run (all-in-one image):

```text
Internet → Cloud Run :8080 (nginx)
              ├─ /        → Vue dist
              └─ /api/*   → Java 127.0.0.1:8088
                                → hosted Postgres (Cloud SQL or Supabase)
```

Firebase Hosting serves **static Vue only**. Build-time `VITE_API_BASE_URL` must be an absolute Cloud Run URL ending in `/api`. Relative `/api` is only for local Vite and the nginx image.

Secrets stay in Secret Manager. Image does not embed Postgres or passwords. Market data in the image remains MOCK.

---

## 11. Testing (as implemented)

Backend (JUnit 5 + Testcontainers PostgreSQL):

- Unit: indicators, analytics, valuation, score, configurable algorithm, JDBC URL parsing, health
- API/IT: health, instruments, DB foundation, RLS lockdown
- Service: listed metrics, screener

Frontend (Vitest + Vue Test Utils):

- Router, header, health store/service, instrument service, API base URL, home view, screener store, display utils

Definition of done the team uses: compile, tests, documented API, working migrations, no committed secrets, errors handled, docs updated.

There is no load test, no contract test against a live vendor (correct — vendor is not connected), and limited UI coverage of Stock/Watchlist/Portfolio.

---

## 12. Non-negotiable constraints (pass/fail checklist)

Reviewers must mark each **Pass / Fail / Partial** against the **implemented** system.

1. Frontend never calls a market-data vendor or Kite.
2. Controllers never call third-party APIs; they go through ports.
3. NSE symbol is never the primary identifier.
4. Financial metrics are computed in Java, unit-tested, not by an LLM.
5. AI only receives structured precomputed inputs.
6. No production market-data adapter exists until `docs/data-licensing.md` names a signed vendor, contract date, permitted uses (display, cache, store, derive), and adapter class.
7. Kite is not used as a quote/candle source.
8. Orders require an explicit confirm step; algo signals never place orders alone.
9. Backtests are labeled simulated.
10. Secrets (DB, Kite, OpenAI) are not in the Vue bundle.
11. UI positions the product as research/education, not guaranteed advice.
12. User algorithms cannot execute arbitrary code.
13. Raw market history must not be overwritten by adjusted series (Phase 31 not built — score whether the **design** preserves this).
14. No silent interpolation of missing financials in the calculation services.

---

## 13. Known limitations (do not score as if solved)

1. **Licensing gate.** TrueData is the preferred vendor *intent*. Global Datafeeds is runner-up. No signed display/storage contract. Personal vendor API plans are typically internal-use only.
2. **Historical depth.** Vendor default APIs give months of intraday history, not years. Long-horizon research must use daily bars or a paid backfill. Do not invent history.
3. **Demo identity.** All tenants can read/write the same demo user. Coverage queue listing is unscoped.
4. **Open API.** `/api/**` is permitAll. CSRF off. Rate limit is per-instance memory.
5. **Mock finance in code**, while SQL financial tables sit mostly unused by the research path.
6. **Typed DTO inconsistency.** Identification/research GETs are records; workspace/trading POSTs are maps.
7. **No Redis**, so no shared cache, no shared rate limit, no pub/sub for later live quotes.
8. **No real-time path.** Future: vendor WS → our Redis/Timescale → our WS. Browser must never open the vendor socket.
9. **Trading state is process-local.** Confirm IDs do not survive restart or multiple Cloud Run instances.
10. **Universe size is 3 synthetic names.** Search/screener have not been proven at 100–300 live listings.
11. **Auth, billing, news, S3, encrypted broker tokens** are future phases.
12. **Paper trading / virtual portfolios** may be restricted by NSE/vendor rules even after a data license. Delayed data or extra approval may be required.

---

## 14. Roadmap context (do not implement in the review)

Sequential phases. Do not skip. Do not start the next major phase unprompted.

| Phase | Name | Gate |
| --- | --- | --- |
| 0–26 | Audit through production-hardening slice | Working mock research product |
| 27 | Provider selection | Docs only — **complete** |
| 28 | Vendor adapter | **Blocked** on signed license in `docs/data-licensing.md` |
| 29–31 | Historical import, quality, corporate actions | After 28 |
| 32–33 | Real-time pipeline + frontend LIVE flag | After licensed WS |
| 34–37 | Research dataset, backtest engine, walk-forward, production algo | No look-ahead; label OOS |
| 38–44 | Kite auth, portfolio sync, orders, paper, user-confirmed trading | Tokens server-side |
| 45–48 | Observability, security hardening, billing port, limited launch | 100–300 names |

Next authorized engineering phase: **28 only after legal/commercial answers exist**. Until then, improve the mock slice (auth, DTOs, tenancy, tests) without a live vendor.

---

## 15. Design decisions already made (do not relitigate unless unsafe)

- Vue + Spring Boot + PostgreSQL/Timescale + Redis (planned) + REST, not a Python monolith and not Kite-centric market data.
- Internal `instrumentId` over exchange symbols.
- Mock-first market data with an explicit legal stop.
- Calculations in Java; LLM explains only.
- All-in-one Cloud Run image for the demo; Firebase for static hosting.
- Supabase as Postgres, not as a frontend BaaS.
- TradingView Lightweight Charts, not a full TradingView terminal, and not 30+ indicators on day one.
- Configuration-driven algorithms rather than hard-coded stock tips.

---

## 16. Suggested prompt to paste with this file

```text
Review the AlphaLens architecture using ONLY the document above.
Follow section 0 exactly (output format, scorecard weights, current implementation vs roadmap).
Be a skeptical staff architect. Call out legal, tenancy, and DTO issues by name.
Do not propose scraping exchanges or using Kite as market data.
```

If the model also has repository access, it may cite files — but the scores must still reflect **what this brief says is implemented**, verified against code if available.
