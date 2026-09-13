# AlphaLens — Stock Intelligence Platform — Master Development Rule

This is the canonical product and engineering rule. Cursor project rules in `.cursor/rules/` summarize the same constraints for the agent. If they ever diverge, this document wins after an explicit update.

## 1. Role

You are the lead software architect and senior full-stack engineer building a production-grade Indian stock intelligence platform.

The product is an alternative to Tickertape with:

* Stock research
* Interactive charts
* Fundamental analysis
* Technical indicators
* Valuation analysis
* Proprietary stock scoring
* Custom user algorithms
* Stock screening
* Watchlists
* Alerts
* Portfolio analysis
* AI explanations
* User-requested stock coverage
* Zerodha Kite integration for portfolio and trading
* Future support for additional brokers

The product must be built incrementally.

DO NOT attempt to build the entire application in one step.

---

## 2. Core Technology Stack

Use the following architecture unless there is a strong technical reason to change it.

### Frontend

* Vue 3
* TypeScript
* Vite
* Pinia
* Vue Router
* Tailwind CSS
* TradingView Lightweight Charts

### Backend

* Java
* Spring Boot
* Spring Security
* REST APIs
* Gradle

### Database

* PostgreSQL
* TimescaleDB for time-series market data

### Cache

* Redis

### AI

* OpenAI API through the backend only

### Storage

* S3-compatible object storage

### Authentication

Initially:

* Email/password
* Google OAuth

Later:

* Additional authentication providers if required

### Broker

Initial broker:

* Zerodha Kite Connect

IMPORTANT:

Kite is a broker integration.

Do NOT design the entire market-data architecture around Kite.

Market-data licensing and redistribution rights must be treated separately.

---

## 3. Fundamental Architecture

```text
                    DATA SOURCES
                         |
          +--------------+--------------+
          |              |              |
     Market Data     Fundamentals      News
          |              |              |
          +--------------+--------------+
                         |
                         v
                DATA INGESTION LAYER
                         |
                         v
                 NORMALIZATION LAYER
             +-----------+-----------+
             |                       |
             v                       v
       PostgreSQL             TimescaleDB
             |                       |
             +-----------+-----------+
                         |
                         v
                    REDIS CACHE
                         |
                         v
                  DOMAIN SERVICES
                         |
       +-----------------+------------------+
       |                 |                  |
       v                 v                  v
  Chart Engine      Algo Engine       Portfolio Engine
       |                 |                  |
       +-----------------+------------------+
                         |
                         v
                      REST API
                         |
                         v
                   Vue Frontend
                         |
             +-----------+-----------+
             |                       |
             v                       v
        Research UI            Kite Integration
```

Keep these concerns separated.

---

## 4. Development Philosophy

1. Build one phase at a time.
2. Never implement future phases prematurely.
3. Every phase must compile.
4. Every phase must have tests.
5. Every API must have validation.
6. Never put business logic inside Vue components.
7. Never put API credentials in the frontend.
8. Never put Kite API secrets in frontend code.
9. Never expose database credentials to the frontend.
10. Never hard-code market data.
11. Never hard-code stock-specific algorithms into controllers.
12. Prefer configuration-driven algorithms.
13. Keep third-party integrations behind interfaces/adapters.
14. Write documentation as the project evolves.
15. Do not introduce unnecessary dependencies.

---

## 5. Critical Product Rule

The platform is NOT merely a stock-data website.

Its core differentiation is:

```text
DATA
  +
ANALYTICS
  +
ALGORITHMS
  +
EXPLANATION
```

The product should eventually answer:

> "Why is this stock attractive or unattractive?"

and:

> "What changed?"

Do not allow the LLM to calculate financial metrics.

Financial calculations must be deterministic.

The AI layer may explain calculated results.

---

## 6. Third-Party Integration Rules

Every external service must be wrapped behind an internal interface.

Example:

```java
public interface MarketDataProvider {
    StockQuote getQuote(String instrumentId);
    List<Candle> getHistoricalData(
        String instrumentId,
        Timeframe timeframe,
        LocalDate from,
        LocalDate to
    );
}
```

Then create:

```text
MarketDataProvider
       |
       +-- ProviderA
       +-- ProviderB
```

Do NOT directly call a third-party API from controllers.

Same approach for:

```text
NewsProvider
AIProvider
BrokerProvider
NotificationProvider
```

---

## 7. Broker Architecture

```java
public interface BrokerProvider {
    AccountInfo getAccount();
    List<Holding> getHoldings();
    List<Position> getPositions();
    OrderResult placeOrder(OrderRequest request);
    OrderResult modifyOrder(OrderRequest request);
    OrderResult cancelOrder(String orderId);
}
```

Initial implementation: `KiteBrokerProvider`.

Future: `UpstoxBrokerProvider`, `DhanBrokerProvider`, `AngelBrokerProvider`, `FYERSBrokerProvider`.

Do not allow Zerodha-specific classes to leak into the core domain model.

---

## 8. Market Data Rule

Never assume that a broker API can legally be used as the commercial market-data source.

Before implementing production market-data redistribution:

* Identify the data provider.
* Verify licensing.
* Verify redistribution rights.
* Verify commercial usage.
* Document the decision in `/docs/data-licensing.md`.

If licensing is unclear:

STOP the production market-data implementation.

Build against a mock provider instead.

---

## 9. Charting Rule

Use TradingView Lightweight Charts for the initial chart implementation.

The chart must support:

* Candlestick
* Line
* Area
* Volume
* Crosshair
* Zoom
* Pan
* Multiple timeframes

Initial timeframes: `1D`, `1W`, `1M`, `6M`, `1Y`, `5Y`, `MAX`.

Initial technical indicators: `SMA`, `EMA`, `RSI`, `MACD`, `VWAP`, `Bollinger Bands`.

Do not implement 30+ indicators initially.

Chart data must come from our backend.

Frontend must NOT call the external market-data provider directly.

---

## 10. Stock Instrument Model

Never use NSE symbol as the primary identifier.

Canonical instrument:

```text
Instrument
---------------------
id
isin
nseSymbol
bseSymbol
companyName
shortName
sector
industry
exchange
status
createdAt
updatedAt
```

Use the internal `instrumentId` everywhere internally.

---

## 11. Phase Execution System

The project must be executed sequentially.

The developer must always identify the current phase before making changes.

Never skip phases.

---

## PHASE 0 — Project Audit

Before writing code:

1. Inspect repository.
2. Identify frontend.
3. Identify backend.
4. Identify existing database.
5. Identify existing authentication.
6. Identify existing dependencies.
7. Identify build system.
8. Identify environment configuration.
9. Identify existing tests.
10. Identify existing Docker configuration.

Create:

```text
/docs/project-audit.md
```

Do not modify architecture until the audit is complete.

Output:

```text
Current architecture
Existing components
Missing components
Recommended architecture
Risks
Next phase
```

---

## PHASE 1 — Repository Foundation

Create/standardize:

```text
/frontend
/backend
/docs
/infrastructure
/scripts
```

Backend packages: `controller`, `service`, `repository`, `domain`, `dto`, `mapper`, `config`, `exception`, `integration`.

Frontend: `components`, `views`, `stores`, `services`, `router`, `types`, `composables`, `layouts`.

Add: `.env.example`, `.gitignore`, Docker configuration, README, local development instructions.

Acceptance criteria:

* Frontend runs.
* Backend runs.
* Database connects.
* Health endpoint works.
* Frontend can call backend.

---

## PHASE 2 — Database Foundation

Implement PostgreSQL.

Create: `users`, `instruments`, `exchanges`, `sectors`, `industries`.

Create migrations. Never modify production schema manually. Use migration tooling.

Acceptance criteria:

* Migration runs from clean database.
* Application starts from clean database.
* Integration tests pass.

---

## PHASE 3 — Instrument Master

Implement `InstrumentService`, `InstrumentRepository`, `InstrumentController`.

APIs:

```http
GET /api/instruments
GET /api/instruments/{id}
GET /api/instruments/search?q=HAL
```

Implement pagination and search indexes.

Acceptance criteria: search works; duplicate instruments are prevented; NSE/BSE/ISIN mapping works.

---

## PHASE 4 — Market Data Abstraction

Create `MarketDataProvider` with a mock implementation first.

Support: Quote, Candle, HistoricalData.

Create: Quote, Candle, Timeframe, Market.

Do NOT connect a production provider yet unless licensing has been confirmed.

Acceptance criteria: mock data works; REST API works; provider can later be replaced without changing frontend/domain logic.

---

## PHASE 5 — TimescaleDB

Store: `instrument_id`, `timestamp`, `open`, `high`, `low`, `close`, `volume`.

Create appropriate indexes. Implement retention/compression only when required.

Acceptance criteria: historical candles can be stored and retrieved efficiently; tests exist for date/time ranges.

---

## PHASE 6 — Chart API

```http
GET /api/instruments/{id}/candles?timeframe=1D&from=2026-01-01&to=2026-09-01
```

Return only normalized internal data.

Acceptance criteria: valid OHLCV; pagination/range limitations; invalid timeframes rejected.

---

## PHASE 7 — Frontend Chart

Reusable `StockChart.vue` with props: `instrumentId`, `timeframe`, `chartType`.

Support candlestick, volume, crosshair, zoom, pan, timeframe selector.

Do NOT implement indicators yet.

Acceptance criteria: chart renders backend data; no third-party market-data API from frontend; chart remains responsive.

---

## PHASE 8 — Technical Indicators

Backend indicator service: SMA, EMA, RSI, MACD, VWAP, Bollinger Bands.

Indicators must be deterministic and unit-tested. Frontend requests indicator data from the backend.

---

## PHASE 9 — Fundamental Data

Normalized models: FinancialPeriod, IncomeStatement, BalanceSheet, CashFlowStatement, Shareholding, Dividend.

Metrics: Revenue, EBITDA, EBIT, PAT, EPS, FCF, ROE, ROCE, Debt, Cash, Debt/Equity, PromoterHolding, PromoterPledge.

All derived metrics must be calculated through backend services.

---

## PHASE 10 — Fundamental Analytics

Revenue CAGR, EPS CAGR, EBITDA Margin, PAT Margin, ROE, ROCE, FCF Conversion, Debt Trend, Margin Trend, Promoter Trend.

Every calculation requires unit tests. Handle nulls, negatives, zero denominators, missing periods, corporate actions. Do not silently generate misleading values.

---

## PHASE 11 — Valuation Engine

PE, PB, EV/EBITDA, PEG, Historical PE percentile, Sector valuation comparison, DCF.

DCF must be transparent. Store assumptions: growth, terminal growth, discount rate, FCF, shares outstanding, net debt. Never hide DCF assumptions from users.

---

## PHASE 12 — Algo Engine

```java
interface StockAlgorithm {
    AlgorithmResult evaluate(StockContext context);
}
```

Configurable rules. Do not hard-code individual stock recommendations.

Return: score, factors, passedRules, failedRules, explanationData.

---

## PHASE 13 — Stock Score

Growth, Quality, Financial, Valuation, Momentum, Risk. Overall 0–100. Weights configurable. Never hide component scores.

---

## PHASE 14 — Stock Page

Stock Header, Price, Change, Chart, Algo Score, Fundamentals, Valuation, Financials, Ownership, Dividends, News, Risk, Algorithm Matches.

This is the most important product screen. Keep it clean.

---

## PHASE 15 — Watchlist

Create watchlist; add/remove/reorder stock; watchlist score. Multiple watchlists allowed.

---

## PHASE 16 — Alerts

Price, score change, valuation, technical, fundamental, result alerts. Use a notification abstraction.

---

## PHASE 17 — Coverage Request

Request stock coverage. Store `user_id`, `instrument_id`, `requested_at`, `status`. Admin dashboard for most-requested stocks and coverage queue.

---

## PHASE 18 — User Algorithms

Controlled rule DSL/JSON. Allow run/save/edit/duplicate/delete/monitor. Do not allow arbitrary code execution.

---

## PHASE 19 — Screener

Filters: market cap, revenue growth, EPS growth, ROCE, ROE, PE, PB, debt, promoter holding, dividend yield, momentum, algo score. Allow saved screens.

---

## PHASE 20 — AI Explanation Layer

Only now integrate OpenAI via `AIProvider`. AI receives structured data and explains. AI must NOT calculate PE, CAGR, ROCE, DCF, returns, or portfolio allocation.

---

## PHASE 21 — Portfolio Without Broker

Manual upload/import: CSV, Excel, manual entry. Portfolio, PortfolioScore, Concentration, SectorAllocation, ProfitLoss, Valuation, Risk.

---

## PHASE 22 — Kite Integration

Only after portfolio analytics works. `KiteBrokerProvider`: login, holdings, positions, orders, trades. Tokens server-side and encrypted. Never expose `api_secret` or `access_token` to Vue. Explicit consent before connecting a broker.

---

## PHASE 23 — Broker Portfolio Sync

Sync holdings, positions, orders, trades. Scheduled synchronization, caching, event/refresh triggers. Do not continuously hammer the broker API. Handle failures gracefully.

---

## PHASE 24 — Trading

Buy, sell, modify, cancel, order status. Always show a confirmation screen. Never place an order solely because an algorithm generated a signal.

---

## PHASE 25 — Backtesting

CAGR, max drawdown, Sharpe, win rate, volatility, number of trades. Prevent look-ahead bias, survivorship bias, future data leakage, incorrect corporate-action handling. Label simulated/historical results. Never present as guaranteed future performance.

---

## PHASE 26 — Production Hardening

Security: OWASP, authz/authn tests, rate limiting, validation, secret management, encryption, audit logs.

Performance: indexes, query optimization, Redis, pagination, chart-data optimization.

Reliability: health checks, structured logging, monitoring, error tracking, backups.

---

## 27. Testing Rules

Every feature must include tests.

Backend: unit, integration, repository, API.

Frontend: component, store, API.

Critical financial calculations require tests with known expected values.

Never mark a phase complete if tests are failing.

---

## 28. Definition of Done

A phase is DONE only when:

1. Code is implemented.
2. Code compiles.
3. Tests pass.
4. API contracts are documented.
5. Database migrations work.
6. No secrets are committed.
7. Error handling exists.
8. README/docs are updated.
9. Existing functionality still works.
10. Acceptance criteria are verified.

---

## 29. Cursor Execution Protocol

1. Inspect relevant files first.
2. Explain current state, files affected, plan, dependencies, risks.
3. Implement the smallest correct change.
4. Run relevant tests.
5. Fix failures before continuing.
6. Verify compilation, tests, API, migration, frontend build.
7. Update documentation if architecture changed.
8. Report Implemented, Files changed, Tests, Known limitations, Next phase.

---

## 30. Strict Phase Rule

NEVER implement multiple major phases automatically.

Finish the current phase, report completion, recommend the next phase, and wait.

---

## 31. Dependency Rule

Before adding a package, ask: is it necessary; is there already a dependency that solves it; is it maintained; does the license allow commercial use; does it increase vendor lock-in; is there a simpler implementation?

---

## 32. Financial Data Integrity

Never fabricate market data; silently interpolate missing financial values; calculate using future data; mix quarterly and annual periods incorrectly; ignore stock splits, bonuses, dividends where relevant, or symbol changes.

All imported financial data should have: `source`, `source_timestamp`, `period`, `reported_date`, `currency`, `unit`.

---

## 33. Regulatory / Product Safety

Initially position the product as research, analytics, screening, portfolio analytics, and educational information.

Avoid casually presenting algorithmic output as guaranteed investment advice.

Personalized investment advice, automated trading, or trade recommendations must be reviewed for applicable Indian securities regulations before production release.

---

## 34. UI Principles

The UI should feel closer to Bloomberg simplicity + Tickertape accessibility + modern SaaS.

Avoid clutter.

Stock page priority: Price → Chart → Score → Why → Fundamentals → Valuation → Risks → News.

Use consistent terminology throughout the application.

---

## 35. Product North Star

The application should eventually answer four questions immediately:

1. Is this company good? — Fundamental score.
2. Is the stock attractively valued? — Valuation engine.
3. Is the timing interesting? — Technical/momentum engine.
4. What changed? — Event + financial + algorithm change detection.

---

## 36. First Development Task

When this rule is installed, DO NOT immediately start coding the entire product.

Start with PHASE 0 — PROJECT AUDIT.

After the audit, stop and wait for approval to begin PHASE 1.
