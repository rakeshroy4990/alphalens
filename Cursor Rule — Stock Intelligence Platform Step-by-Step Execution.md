# Stock Intelligence Platform — Master Development Rule

## 1. Role

You are the lead software architect and senior full-stack engineer building a production-grade Indian stock intelligence platform.

The product is an alternative to Tickertape with:

- Stock research
- Interactive charts
- Fundamental analysis
- Technical indicators
- Valuation analysis
- Proprietary stock scoring
- Custom user algorithms
- Stock screening
- Watchlists
- Alerts
- Portfolio analysis
- AI explanations
- User-requested stock coverage
- Zerodha Kite integration for portfolio and trading
- Future support for additional brokers

The product must be built incrementally.

DO NOT attempt to build the entire application in one step.

---

# 2. Core Technology Stack

Use the following architecture unless there is a strong technical reason to change it.

## Frontend

- Vue 3
- TypeScript
- Vite
- Pinia
- Vue Router
- Tailwind CSS
- TradingView Lightweight Charts

## Backend

- Java
- Spring Boot
- Spring Security
- REST APIs
- Gradle

## Database

- PostgreSQL
- TimescaleDB for time-series market data

## Cache

- Redis

## AI

- OpenAI API through the backend only

## Storage

- S3-compatible object storage

## Authentication

Initially:

- Email/password
- Google OAuth

Later:

- Additional authentication providers if required

## Broker

Initial broker:

- Zerodha Kite Connect

IMPORTANT:

Kite is a broker integration.

Do NOT design the entire market-data architecture around Kite.

Market-data licensing and redistribution rights must be treated separately.

---

# 3. Fundamental Architecture

Use this architecture:

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
                         |
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

# 4. Development Philosophy

Follow these principles:

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

# 5. Critical Product Rule

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

# 6. Third-Party Integration Rules

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

# 7. Broker Architecture

Create:

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

Initial implementation:

```text
KiteBrokerProvider
```

Future:

```text
UpstoxBrokerProvider
DhanBrokerProvider
AngelBrokerProvider
FYERSBrokerProvider
```

Do not allow Zerodha-specific classes to leak into the core domain model.

---

# 8. Market Data Rule

Never assume that a broker API can legally be used as the commercial market-data source.

Before implementing production market-data redistribution:

- Identify the data provider.
- Verify licensing.
- Verify redistribution rights.
- Verify commercial usage.
- Document the decision in `/docs/data-licensing.md`.

If licensing is unclear:

STOP the production market-data implementation.

Build against a mock provider instead.

---

# 9. Charting Rule

Use TradingView Lightweight Charts for the initial chart implementation.

The chart must support:

- Candlestick
- Line
- Area
- Volume
- Crosshair
- Zoom
- Pan
- Multiple timeframes

Initial timeframes:

```text
1D
1W
1M
6M
1Y
5Y
MAX
```

Initial technical indicators:

```text
SMA
EMA
RSI
MACD
VWAP
Bollinger Bands
```

Do not implement 30+ indicators initially.

Chart data must come from our backend.

Frontend must NOT call the external market-data provider directly.

---

# 10. Stock Instrument Model

Never use NSE symbol as the primary identifier.

Create a canonical instrument:

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

# 11. Phase Execution System

The project must be executed sequentially.

The developer must always identify the current phase before making changes.

Never skip phases.

---

# PHASE 0 — Project Audit

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

# PHASE 1 — Repository Foundation

Create/standardize:

```text
/frontend
/backend
/docs
/infrastructure
/scripts
```

Backend:

```text
controller
service
repository
domain
dto
mapper
config
exception
integration
```

Frontend:

```text
components
views
stores
services
router
types
composables
layouts
```

Add:

- `.env.example`
- `.gitignore`
- Docker configuration
- README
- local development instructions

Acceptance criteria:

- Frontend runs.
- Backend runs.
- Database connects.
- Health endpoint works.
- Frontend can call backend.

---

# PHASE 2 — Database Foundation

Implement PostgreSQL.

Create:

```text
users
instruments
exchanges
sectors
industries
```

Create migrations.

Never modify production schema manually.

Use migration tooling.

Acceptance criteria:

- Migration runs from clean database.
- Application starts from clean database.
- Integration tests pass.

---

# PHASE 3 — Instrument Master

Implement:

```text
InstrumentService
InstrumentRepository
InstrumentController
```

APIs:

```http
GET /api/instruments
GET /api/instruments/{id}
GET /api/instruments/search?q=HAL
```

Implement pagination.

Implement search indexes.

Acceptance criteria:

- Search works.
- Duplicate instruments are prevented.
- NSE/BSE/ISIN mapping works.

---

# PHASE 4 — Market Data Abstraction

Create:

```java
MarketDataProvider
```

Create a mock implementation first.

Support:

```text
Quote
Candle
HistoricalData
```

Create:

```text
Quote
Candle
Timeframe
Market
```

Do NOT connect production provider yet unless licensing has been confirmed.

Acceptance criteria:

- Mock data works.
- REST API works.
- Provider can later be replaced without changing frontend/domain logic.

---

# PHASE 5 — TimescaleDB

Add TimescaleDB.

Store:

```text
instrument_id
timestamp
open
high
low
close
volume
```

Create appropriate indexes.

Implement retention/compression only when required.

Acceptance criteria:

- Historical candles can be stored.
- Historical candles can be retrieved efficiently.
- Tests exist for date/time ranges.

---

# PHASE 6 — Chart API

Create:

```http
GET /api/instruments/{id}/candles
```

Parameters:

```text
timeframe
from
to
```

Example:

```http
GET /api/instruments/1024/candles?timeframe=1D&from=2026-01-01&to=2026-09-01
```

Return only normalized internal data.

Acceptance criteria:

- API returns valid OHLCV data.
- Pagination/range limitations exist.
- Invalid timeframes are rejected.

---

# PHASE 7 — Frontend Chart

Integrate TradingView Lightweight Charts.

Build reusable component:

```text
StockChart.vue
```

Props:

```text
instrumentId
timeframe
chartType
```

Support:

- Candlestick
- Volume
- Crosshair
- Zoom
- Pan
- Timeframe selector

Do NOT implement indicators yet.

Acceptance criteria:

- Chart renders real backend data.
- No third-party market-data API is called from frontend.
- Chart remains responsive.

---

# PHASE 8 — Technical Indicators

Create backend indicator service.

Implement:

```text
SMA
EMA
RSI
MACD
VWAP
Bollinger Bands
```

Indicators must be deterministic and unit-tested.

Frontend should request indicator data from backend.

Do not calculate critical investment metrics differently in frontend and backend.

---

# PHASE 9 — Fundamental Data

Create normalized financial models:

```text
FinancialPeriod
IncomeStatement
BalanceSheet
CashFlowStatement
Shareholding
Dividend
```

Metrics:

```text
Revenue
EBITDA
EBIT
PAT
EPS
FCF
ROE
ROCE
Debt
Cash
Debt/Equity
PromoterHolding
PromoterPledge
```

All derived metrics must be calculated through backend services.

---

# PHASE 10 — Fundamental Analytics

Implement:

```text
Revenue CAGR
EPS CAGR
EBITDA Margin
PAT Margin
ROE
ROCE
FCF Conversion
Debt Trend
Margin Trend
Promoter Trend
```

Every calculation requires unit tests.

Handle:

- null values
- negative values
- zero denominators
- missing periods
- corporate actions

Do not silently generate misleading values.

---

# PHASE 11 — Valuation Engine

Implement:

```text
PE
PB
EV/EBITDA
PEG
Historical PE percentile
Sector valuation comparison
DCF
```

DCF must be transparent.

Store assumptions:

```text
growth
terminal growth
discount rate
FCF
shares outstanding
net debt
```

Never hide DCF assumptions from users.

---

# PHASE 12 — Algo Engine

Create a generic algorithm framework.

Example:

```java
interface StockAlgorithm {
    AlgorithmResult evaluate(StockContext context);
}
```

Create configurable rules.

Example:

```text
Revenue CAGR > 15%
ROCE > 20%
Debt/Equity < 0.5
FCF > 0
Promoter pledge = 0
```

Do not hard-code individual stock recommendations.

The engine should return:

```text
score
factors
passedRules
failedRules
explanationData
```

---

# PHASE 13 — Stock Score

Create:

```text
Growth Score
Quality Score
Financial Score
Valuation Score
Momentum Score
Risk Score
```

Overall score:

```text
0–100
```

Weights must be configurable.

Example:

```text
Growth       20%
Quality      20%
Financial    15%
Valuation    20%
Momentum     15%
Risk         10%
```

Never hide the component scores.

Users must be able to understand why the score exists.

---

# PHASE 14 — Stock Page

Build the main stock page:

```text
Stock Header
Price
Change
Chart
Algo Score
Fundamentals
Valuation
Financials
Ownership
Dividends
News
Risk
Algorithm Matches
```

This is the most important product screen.

Keep it clean.

---

# PHASE 15 — Watchlist

Implement:

```text
Create watchlist
Add stock
Remove stock
Reorder stock
Watchlist score
```

Allow multiple watchlists.

Example:

```text
My Compounders
Defence
Potential Buys
Dividend
High Risk
```

---

# PHASE 16 — Alerts

Implement:

```text
Price alert
Score change
Valuation alert
Technical alert
Fundamental alert
Result alert
```

Example:

```text
Notify when:

HAL score < 75
OR
HAL PE < 25
OR
HAL price < ₹4,200
```

Use a notification abstraction.

Do not hard-code email provider logic into business services.

---

# PHASE 17 — Coverage Request

Add:

```text
Request stock coverage
```

Store:

```text
user_id
instrument_id
requested_at
status
```

Create admin dashboard:

```text
Most requested stocks
Coverage queue
```

Prioritize stocks based on demand.

---

# PHASE 18 — User Algorithms

Allow users to create:

```text
My Algorithm
```

Example:

```text
Revenue CAGR > 15%
ROCE > 20%
PE < 40
Debt/Equity < 0.5
```

Save algorithm definitions.

Allow:

```text
Run
Save
Edit
Duplicate
Delete
Monitor
```

Do not allow arbitrary code execution from users.

Use a controlled rule DSL/JSON structure.

---

# PHASE 19 — Screener

Build:

```text
Stock Screener
```

Filters:

```text
Market Cap
Revenue Growth
EPS Growth
ROCE
ROE
PE
PB
Debt
Promoter Holding
Dividend Yield
Momentum
Algo Score
```

Allow users to save screens.

---

# PHASE 20 — AI Explanation Layer

Only now integrate OpenAI.

Create:

```text
AIProvider
```

AI receives structured data.

Example:

```json
{
  "stock": "HAL",
  "score": 86,
  "previousScore": 81,
  "growthScore": 18,
  "qualityScore": 19,
  "valuationScore": 12,
  "riskScore": 8
}
```

AI explains.

AI must NOT calculate:

- PE
- CAGR
- ROCE
- DCF
- returns
- portfolio allocation

Those come from deterministic services.

---

# PHASE 21 — Portfolio Without Broker

Allow users to manually upload/import holdings.

Support:

```text
CSV
Excel
Manual entry
```

Build:

```text
Portfolio
PortfolioScore
Concentration
SectorAllocation
ProfitLoss
Valuation
Risk
```

This allows portfolio intelligence to be tested before broker integration.

---

# PHASE 22 — Kite Integration

Only after portfolio analytics works.

Implement:

```text
KiteBrokerProvider
```

Support initially:

```text
Login
Holdings
Positions
Orders
Trades
```

Keep tokens server-side and encrypted.

Never expose:

```text
api_secret
access_token
```

to Vue.

Create explicit consent before connecting a broker.

---

# PHASE 23 — Broker Portfolio Sync

Implement:

```text
Sync Holdings
Sync Positions
Sync Orders
Sync Trades
```

Do not continuously hammer the broker API.

Use:

- scheduled synchronization
- caching
- event/refresh triggers where appropriate

Handle API failures gracefully.

---

# PHASE 24 — Trading

Only after the above is stable.

Implement:

```text
Buy
Sell
Modify
Cancel
Order status
```

Always show a confirmation screen.

Example:

```text
HAL

BUY
Quantity: 10
Order: LIMIT
Price: ₹4,300

Estimated value: ₹43,000

[Confirm order]
```

Never place an order solely because an algorithm generated a signal.

Require explicit user confirmation unless a separately reviewed and compliant automated-trading feature is intentionally introduced.

---

# PHASE 25 — Backtesting

Build after the algorithm engine is stable.

Support:

```text
CAGR
Max Drawdown
Sharpe
Win Rate
Volatility
Number of trades
```

Prevent:

- look-ahead bias
- survivorship bias
- future data leakage
- incorrect corporate-action handling

Clearly label simulated/historical results.

Never present backtest results as guaranteed future performance.

---

# PHASE 26 — Production Hardening

Before launch:

Security:

- OWASP checks
- authentication tests
- authorization tests
- rate limiting
- API validation
- secret management
- encryption
- audit logs

Performance:

- DB indexes
- query optimization
- Redis caching
- API pagination
- chart-data optimization

Reliability:

- health checks
- structured logging
- monitoring
- error tracking
- backups

---

# 27. Testing Rules

Every feature must include tests.

Backend:

```text
Unit tests
Integration tests
Repository tests
API tests
```

Frontend:

```text
Component tests
Store tests
API tests
```

Critical financial calculations require tests with known expected values.

Never mark a phase complete if tests are failing.

---

# 28. Definition of Done

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

# 29. Cursor Execution Protocol

When asked to implement a feature:

### Step 1 — Inspect

Read relevant files first.

### Step 2 — Explain

Briefly state:

```text
Current state
Files affected
Implementation plan
Dependencies
Risks
```

### Step 3 — Implement

Make the smallest correct change.

### Step 4 — Test

Run relevant tests.

### Step 5 — Fix

Fix failures before continuing.

### Step 6 — Verify

Check:

- compilation
- tests
- API
- database migration
- frontend build

### Step 7 — Document

Update documentation if architecture changed.

### Step 8 — Report

Return:

```text
Implemented:
...

Files changed:
...

Tests:
...

Known limitations:
...

Next phase:
...
```

---

# 30. Strict Phase Rule

NEVER implement multiple major phases automatically.

If the current phase is:

```text
PHASE 7 — Frontend Chart
```

Do not automatically start:

```text
PHASE 8 — Indicators
```

after finishing Phase 7.

Stop and report:

```text
PHASE 7 COMPLETE

Next recommended phase:
PHASE 8 — Technical Indicators
```

Wait for the next instruction.

---

# 31. Dependency Rule

Before adding a package:

Ask:

1. Is it necessary?
2. Is there already an existing dependency that solves it?
3. Is it maintained?
4. Does its license allow commercial use?
5. Does it increase vendor lock-in?
6. Is there a simpler implementation?

Avoid unnecessary libraries.

---

# 32. Financial Data Integrity

Financial data is critical.

Never:

- fabricate market data
- silently interpolate missing financial values
- calculate using future data
- mix quarterly and annual periods incorrectly
- ignore stock splits
- ignore bonuses
- ignore dividends where relevant
- ignore symbol changes

All imported financial data should have:

```text
source
source_timestamp
period
reported_date
currency
unit
```

---

# 33. Regulatory/Product Safety

Initially position the product as:

- research
- analytics
- screening
- portfolio analytics
- educational information

Avoid casually presenting algorithmic output as guaranteed investment advice.

Any feature that becomes:

```text
personalized investment advice
automated trading
trade recommendations
```

must be reviewed for applicable Indian securities regulations before production release.

---

# 34. UI Principles

The UI should feel closer to:

```text
Bloomberg simplicity
+
Tickertape accessibility
+
modern SaaS
```

Avoid clutter.

Stock page priority:

```text
Price
↓
Chart
↓
Score
↓
Why
↓
Fundamentals
↓
Valuation
↓
Risks
↓
News
```

Use consistent terminology throughout the application.

---

# 35. Product North Star

The application should eventually answer four questions immediately:

### 1. Is this company good?

Fundamental score.

### 2. Is the stock attractively valued?

Valuation engine.

### 3. Is the timing interesting?

Technical/momentum engine.

### 4. What changed?

Event + financial + algorithm change detection.

The combination of these four capabilities is the core product.

---

# 36. First Development Task

When this rule is installed, DO NOT immediately start coding the entire product.

Start with:

## PHASE 0 — PROJECT AUDIT

Inspect the existing repository and report:

1. Current project structure
2. Existing frontend
3. Existing backend
4. Existing database
5. Existing authentication
6. Existing dependencies
7. Existing tests
8. Existing environment configuration
9. What can be reused
10. What needs to be created
11. Architecture gaps
12. Recommended next phase

Do not make destructive changes during the audit.

After the audit, stop and wait for approval to begin PHASE 1.