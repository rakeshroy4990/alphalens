# Stock Intelligence Platform — Remaining Development Phases

## 1. Current Project Status

The following phases are COMPLETE:

- Project audit
- Repository foundation
- Database foundation
- Stock Information & Identification
- Market-data abstraction
- TimescaleDB foundation
- Chart API
- Frontend chart
- Technical indicators
- Fundamental data
- Fundamental analytics
- Valuation engine
- Algo engine
- Stock score
- Stock page
- Watchlists
- Alerts
- Coverage requests
- User algorithms
- Screener
- AI explanation layer
- Manual portfolio
- Existing application functionality

The application is now entering the **production data + research + broker integration stage**.

DO NOT rebuild completed phases.

DO NOT replace existing architecture unless there is a documented technical reason.

---

# 2. Remaining Roadmap

Execute the remaining work in exactly this order:

```text
PHASE 27  Market Data Provider Selection
PHASE 28  Market Data Provider Integration
PHASE 29  Historical Data Import
PHASE 30  Historical Data Validation
PHASE 31  Corporate Actions & Adjustments
PHASE 32  Real-Time Market Data Pipeline
PHASE 33  Real-Time Frontend Streaming
PHASE 34  Algo Research Dataset
PHASE 35  Backtesting Engine
PHASE 36  Walk-Forward Validation
PHASE 37  Production Algo Engine
PHASE 38  Kite Developer/Broker Setup
PHASE 39  Kite Authentication
PHASE 40  Kite Portfolio Integration
PHASE 41  Real-Time Portfolio
PHASE 42  Kite Order Integration
PHASE 43  Paper Trading
PHASE 44  Algo + User-Confirmed Trading
PHASE 45  Monitoring & Observability
PHASE 46  Security & Production Hardening
PHASE 47  Billing / Subscription
PHASE 48  Production Launch
```

NEVER skip a phase.

NEVER implement several major phases in one request.

Complete one phase, test it, document it, and STOP.

---

# 3. Critical Third-Party Integration Principle

Third-party integrations must NEVER leak into the core application architecture.

Use adapters/interfaces.

Example:

```java
public interface MarketDataProvider {

    Quote getQuote(String instrumentId);

    List<Candle> getHistoricalCandles(
        String instrumentId,
        Timeframe timeframe,
        Instant from,
        Instant to
    );

    void subscribeQuotes(
        List<String> instrumentIds,
        QuoteListener listener
    );
}
```

The rest of the application must depend on:

```text
MarketDataProvider
```

NOT:

```text
TrueData
Kite
Upstox
Angel
```

specific classes.

---

# 4. Third-Party Integration Registry

Create:

```text
/docs/integrations.md
```

Maintain a table:

| Integration | Purpose | Environment | Status | Credentials | Commercial Rights |
|---|---|---|---|---|---|
| Market Data Provider | Historical + realtime | Production | Pending/Active | Server | Must verify |
| TradingView Lightweight Charts | Chart rendering | Frontend | Active | None | Verify license |
| OpenAI | AI explanations | Production | Active | Server | Active |
| Zerodha Kite | Broker | Production | Pending | Server | Must verify |
| Email Provider | Alerts | Production | Pending | Server | Verify |
| Payment Provider | Subscription | Production | Pending | Server | Verify |
| Analytics | Product analytics | Production | Pending | Client/Server | Verify |

Update this file whenever an integration is added or changed.

---

# 5. Third-Party Integration Protocol

Before integrating ANY external service, follow these steps.

## Step 1 — Identify requirement

Document:

```text
Why do we need this integration?
What data/functionality does it provide?
Can we build this ourselves?
Is the integration required for MVP?
```

## Step 2 — Research official documentation

Use the provider's official documentation.

Do NOT rely on:

- random blogs
- outdated tutorials
- Stack Overflow alone
- unofficial SDK examples

## Step 3 — Check commercial licensing

Determine:

- Personal use allowed?
- Commercial use allowed?
- Data redistribution allowed?
- Displaying data to end users allowed?
- API usage limits?
- Number of users allowed?
- Caching allowed?
- Historical storage allowed?
- WebSocket redistribution allowed?
- Derived data allowed?

If unclear:

STOP production integration.

Create:

```text
/docs/integration-<provider>-questions.md
```

## Step 4 — Credential architecture

Determine:

```text
Public credential
Server credential
OAuth credential
API secret
Access token
Refresh token
```

Secrets must remain server-side.

## Step 5 — Create adapter

Example:

```text
MarketDataProvider
        |
        +--- ProviderAdapter
```

## Step 6 — Create configuration

Use environment variables.

Example:

```text
MARKET_DATA_API_KEY=
MARKET_DATA_API_SECRET=
MARKET_DATA_BASE_URL=
```

Never commit real credentials.

## Step 7 — Implement health check

Every third-party integration must have a health/status check.

Example:

```text
GET /internal/integrations/market-data/health
```

## Step 8 — Implement timeout/retry

External API failures must not bring down the application.

Implement:

- connection timeout
- request timeout
- limited retry
- exponential backoff where appropriate
- circuit breaker where appropriate

## Step 9 — Implement rate limiting

Respect provider limits.

Never create uncontrolled loops against an external API.

## Step 10 — Implement logging

Log:

```text
provider
operation
duration
status
error category
request correlation ID
```

NEVER log:

```text
API secret
access token
refresh token
password
full authorization header
```

## Step 11 — Write integration tests

Use mocks/stubs where possible.

Real API tests should be limited and controlled.

## Step 12 — Document

Update:

```text
/docs/integrations.md
```

and provider-specific documentation.

---

# 6. PHASE 27 — Market Data Provider Selection

Goal:

Select the production market-data provider.

Required:

### Real-time

- NSE
- BSE
- LTP
- OHLC
- volume
- WebSocket
- scalable subscriptions

### Historical

- Daily
- Intraday
- 1-minute
- 5-minute
- 15-minute
- 30-minute
- 1-hour where available
- 5+ years daily
- sufficient intraday history for research

### Corporate actions

- Splits
- Bonus
- Rights
- Dividends
- mergers
- symbol changes
- ISIN changes

### Commercial requirements

Confirm:

- commercial use
- display to users
- redistribution
- caching
- historical storage
- derived analytics
- number of users
- WebSocket usage

Potential providers may include authorized market-data vendors such as TrueData or other exchange-authorized providers.

Do NOT assume any provider is commercially suitable without checking its current terms.

Deliverable:

```text
/docs/market-data-provider-decision.md
```

Include:

```text
Provider
Pricing
Data coverage
Latency
API
WebSocket
Historical data
Corporate actions
Commercial license
Limitations
Decision
```

STOP after Phase 27.

---

# 7. PHASE 28 — Market Data Provider Integration

Implement:

```text
MarketDataProvider
```

Then:

```text
SelectedMarketDataProvider
```

Required services:

```text
QuoteService
HistoricalDataService
InstrumentMappingService   # vendor symbol → AlphaLens stock identification (`instrumentId`)
MarketStatusService
```

Never expose the vendor's raw response directly to the frontend.

Normalize everything into internal models.

Example:

```java
Quote {
    instrumentId
    timestamp
    lastPrice
    open
    high
    low
    previousClose
    volume
}
```

---

# 8. PHASE 29 — Historical Data Import

Build a controlled import process.

```text
Provider
   ↓
Downloader
   ↓
Parser
   ↓
Validator
   ↓
Normalizer
   ↓
Database
```

Support:

```text
Daily
1m
5m
15m
```

Start with the initial stock universe.

Do NOT download the entire Indian market until the system has been validated.

Create an import job:

```text
HistoricalImportJob
```

Track:

```text
job_id
instrument   # stock identification id
timeframe
from
to
status
records_processed
records_failed
started_at
completed_at
```

---

# 9. PHASE 30 — Historical Data Validation

Before using historical data for algorithms:

Check:

- duplicate candles
- missing candles
- invalid OHLC
- negative volume
- timestamp errors
- timezone errors
- market-session errors
- corporate-action inconsistencies
- abnormal price jumps

Create:

```text
DataQualityService
```

Output:

```text
Coverage: 99.7%
Missing candles: 124
Duplicate candles: 0
Invalid candles: 0
```

Do NOT allow low-quality data into production backtests without an explicit warning.

---

# 10. PHASE 31 — Corporate Actions

Create:

```text
CorporateAction
```

Support:

```text
SPLIT
BONUS
RIGHTS
DIVIDEND
MERGER
DEMERGER
SYMBOL_CHANGE
ISIN_CHANGE
```

Historical prices used for research must be adjusted appropriately.

Keep both:

```text
raw data
adjusted data
```

Do not destroy raw provider data.

---

# 11. PHASE 32 — Real-Time Market Data Pipeline

Architecture:

```text
Market Data Provider
        |
     WebSocket
        |
        v
MarketDataConsumer
        |
        v
Normalizer
        |
        +------> Redis
        |
        +------> TimescaleDB
        |
        v
Internal WebSocket
        |
        v
Frontend
```

The browser must NOT connect directly to the market-data vendor.

Implement:

- reconnect
- heartbeat
- stale-data detection
- subscription management
- backpressure
- connection monitoring

---

# 12. PHASE 33 — Real-Time Frontend Streaming

Update:

```text
StockChart.vue
```

Real-time updates must:

- update current candle
- update LTP
- update volume
- avoid full chart reload
- reconnect automatically
- show connection status

Display:

```text
LIVE
DELAYED
DISCONNECTED
```

Never display "LIVE" if the data stream is stale.

---

# 13. PHASE 34 — Algo Research Dataset

Create a dedicated research layer.

Do NOT run backtests directly against production API endpoints.

Architecture:

```text
Historical Data
       ↓
Research Dataset
       ↓
Feature Engine
       ↓
Algo
       ↓
Backtester
```

Features may include:

```text
SMA
EMA
RSI
MACD
ROCE
ROE
Revenue CAGR
EPS CAGR
PE
PB
Debt
FCF
Momentum
Volume
```

Every feature must have:

```text
name
value
timestamp
source
calculation version
```

---

# 14. PHASE 35 — Backtesting Engine

Support:

```text
Entry
Exit
Position sizing
Cash
Transaction cost
Slippage
Brokerage
Taxes
Rebalancing
```

Results:

```text
CAGR
Absolute return
Max drawdown
Sharpe
Sortino
Win rate
Profit factor
Volatility
Number of trades
```

Never use future information.

Never allow look-ahead bias.

Never use today's financial data to simulate a decision that occurred before that data was published.

---

# 15. PHASE 36 — Walk-Forward Validation

Every serious algorithm should support:

```text
Training period
Validation period
Out-of-sample period
```

Example:

```text
2015–2020 Training
2021–2022 Validation
2023–2026 Out-of-sample
```

Show users clearly:

```text
Backtest
Out-of-sample
```

Do not present backtested results as guaranteed future performance.

---

# 16. PHASE 37 — Production Algo Engine

The production algo engine uses current data.

Architecture:

```text
Real-time Data
      ↓
Feature Engine
      ↓
Algorithm
      ↓
Signal
      ↓
Score
      ↓
Alert
```

Example:

```text
HAL

Score: 87

Signal:
ACCUMULATE

Reasons:
- ROCE above threshold
- Revenue growth above threshold
- Price above 200 DMA
- Valuation within configured range
```

Algorithms must be deterministic.

LLMs may explain the signal but must NOT generate the underlying calculation.

---

# 17. PHASE 38 — Kite Setup

Before writing code:

Create:

```text
/docs/kite-integration.md
```

Document:

- Kite Connect plan
- API capabilities
- OAuth flow
- Redirect URL
- rate limits
- access-token lifetime
- WebSocket capabilities
- historical API
- order API
- portfolio API
- commercial/business restrictions
- expected user count

Contact Zerodha/business support if any commercial usage or redistribution issue is unclear.

DO NOT assume a personal API setup is sufficient for your commercial platform.

---

# 18. PHASE 39 — Kite Authentication

Create:

```text
BrokerProvider
KiteBrokerProvider
```

Flow:

```text
User
 ↓
Connect Zerodha
 ↓
Kite Login
 ↓
Authorization
 ↓
Callback
 ↓
Backend
 ↓
Access Token
```

Tokens:

- server-side only
- encrypted
- never logged
- never returned to frontend

Implement:

```text
connect
disconnect
connection status
token expiry
re-authentication
```

---

# 19. PHASE 40 — Kite Portfolio Integration

First integrate read-only functionality.

Retrieve:

```text
Holdings
Positions
Orders
Trades
Account information where permitted
```

Map Kite listings into AlphaLens stock identification (Java `Instrument`):

```text
Instrument
```

Never use Kite's identifier as the AlphaLens stock id (`instrumentId`).

---

# 20. PHASE 41 — Real-Time Portfolio

Combine:

```text
Kite holdings
+
Your real-time market data
```

Calculate:

```text
Current value
Today's P&L
Overall P&L
Allocation
Sector exposure
Concentration
Portfolio score
Valuation
Risk
```

Do not poll Kite unnecessarily for every price update.

---

# 21. PHASE 42 — Kite Order Integration

Only after read-only integration is stable.

Support:

```text
BUY
SELL
MODIFY
CANCEL
ORDER STATUS
```

Always show an order confirmation.

Example:

```text
HAL

BUY 10
LIMIT ₹4,300

Estimated value:
₹43,000

[Confirm]
```

Never allow an algorithm to silently place an order.

---

# 22. PHASE 43 — Paper Trading

Before live execution:

```text
Signal
 ↓
Paper Order
 ↓
Virtual Portfolio
 ↓
Performance
```

Track:

```text
Paper P&L
Win rate
Drawdown
CAGR
Slippage assumptions
```

This is mandatory before enabling advanced algorithmic execution.

---

# 23. PHASE 44 — Algo + User-Confirmed Trading

Initial flow:

```text
Algorithm
    ↓
Signal
    ↓
User Alert
    ↓
Stock Page
    ↓
User presses BUY
    ↓
Order Preview
    ↓
User Confirmation
    ↓
Kite
```

Do NOT initially implement:

```text
Algorithm
   ↓
Automatic order
```

Any future automated trading capability requires separate regulatory and compliance review.

---

# 24. PHASE 45 — Monitoring

Add:

```text
Application monitoring
Database monitoring
Market-data monitoring
WebSocket monitoring
Kite API monitoring
Algorithm job monitoring
```

Track:

```text
API latency
API errors
WebSocket disconnects
Data freshness
Missing candles
Failed imports
Kite failures
Order failures
```

Create dashboards and alerts.

---

# 25. PHASE 46 — Security

Before production:

- OWASP review
- dependency scanning
- secret scanning
- API authentication
- authorization
- rate limiting
- encryption
- audit logs
- database backups
- disaster recovery
- token encryption
- CSRF/CORS review
- WebSocket authentication

Never store broker credentials in plain text.

---

# 26. PHASE 47 — Billing

Only after the core product works.

Possible plans:

```text
FREE
PRO
PRO+
```

Possible payment provider:

- Razorpay
- Stripe if applicable to your business structure

Create:

```text
BillingProvider
```

Do not hard-code Razorpay into subscription business logic.

---

# 27. PHASE 48 — Production Launch

Before launch:

```text
Data provider
       ✓
Historical data
       ✓
Realtime
       ✓
Charts
       ✓
Algorithms
       ✓
Backtesting
       ✓
Kite
       ✓
Portfolio
       ✓
Paper trading
       ✓
Security
       ✓
Monitoring
       ✓
Billing
       ✓
```

Then launch with a limited universe first.

Recommended initial universe:

```text
100–300 stocks
```

Expand coverage based on:

```text
Coverage requests
User demand
Data quality
Algorithm reliability
```

---

# 28. Cursor Execution Rules

For every phase:

## STEP 1 — Inspect

Read the relevant existing implementation.

Do not rewrite working code.

## STEP 2 — Plan

Before coding, report:

```text
Current state
Files affected
New files
Database changes
Third-party changes
Risks
Testing plan
```

## STEP 3 — Implement

Implement only the current phase.

## STEP 4 — Test

Run:

```text
Backend tests
Frontend tests
Integration tests
Build
```

where applicable.

## STEP 5 — Verify

Check:

```text
API
Database
Security
Error handling
Performance
```

## STEP 6 — Document

Update:

```text
/docs/integrations.md
/docs/architecture.md
```

if applicable.

## STEP 7 — STOP

Do NOT automatically proceed to the next phase.

Report:

```text
PHASE XX COMPLETE

Implemented:
...

Files changed:
...

Third-party integrations:
...

Tests:
...

Known issues:
...

Next phase:
PHASE XX+1
```

Wait for the next instruction.

---

# 29. Third-Party Integration STOP Conditions

STOP and ask for clarification if:

1. Commercial licensing is unclear.
2. Data redistribution rights are unclear.
3. API pricing is unclear.
4. API limits may affect architecture.
5. Authentication requirements are unclear.
6. Provider documentation contradicts existing assumptions.
7. A credential is required but not configured.
8. Production access is unavailable.
9. The provider requires a business/commercial agreement.
10. The integration would require changing the core architecture.

Do NOT guess.

Do NOT invent API endpoints.

Do NOT invent request/response fields.

Use the provider's current official documentation.

---

# 30. Credential Rules

Use:

```text
.env
.env.local
secret manager
```

Never commit:

```text
API_KEY
API_SECRET
ACCESS_TOKEN
REFRESH_TOKEN
DATABASE_PASSWORD
OPENAI_KEY
KITE_API_SECRET
```

Repository must contain:

```text
.env.example
```

with placeholder values only.

---

# 31. Data Source Rules

Every production dataset must track:

```text
source
retrievedAt
dataVersion
```

For financial data also track:

```text
reportedDate
periodEnd
publicationDate
```

This is essential for correct historical backtesting.

---

# 32. Do Not Create Vendor Lock-In

Bad:

```text
StockService
 ↓
Kite API
```

Good:

```text
StockService
 ↓
MarketDataProvider
 ↓
KiteMarketDataAdapter
```

Bad:

```text
PortfolioService
 ↓
Kite
```

Good:

```text
PortfolioService
 ↓
BrokerProvider
 ↓
KiteBrokerProvider
```

This allows future support for:

```text
Zerodha
Upstox
Dhan
Angel One
FYERS
```

without changing the core product.

---

# 33. Priority Order

If there is a choice between features, prioritize:

```text
1. Data correctness
2. Data licensing
3. Historical data
4. Real-time data
5. Chart reliability
6. Algo correctness
7. Backtesting correctness
8. Portfolio synchronization
9. Broker authentication
10. Order execution
11. AI features
12. UI enhancements
```

Never prioritize UI polish over data correctness.

---

# 34. Current Starting Point

The project is currently ready to begin:

# PHASE 27 — MARKET DATA PROVIDER SELECTION

The first Cursor task should be:

> Audit the existing market-data interfaces and compare the requirements against potential production providers. Do not modify production code yet. Create `/docs/market-data-provider-decision.md` with the required data capabilities, licensing requirements, API requirements, pricing considerations, and integration risks. Use official provider documentation wherever possible. Stop after completing the analysis.

After Phase 27 is approved, proceed to Phase 28.

Never skip the provider/licensing decision before implementing production market data.