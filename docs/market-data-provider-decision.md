# Phase 27 — Market data provider decision

Status: **PHASE 27 COMPLETE — vendor shortlisted, commercial contract not signed.**

Date: 2026-09-13

This document selects the production market-data path. It does **not** authorize Phase 28 implementation. Production `MarketDataProvider` code stays on `MockMarketDataProvider` until a written commercial + display + storage agreement exists.

Official sources used (not blogs):

- [TrueData Market Data APIs](https://www.truedata.in/market-data-apis)
- [TrueData Market Data API product](https://www.truedata.in/products/marketdataapi)
- [TrueData historical availability](https://feedback.truedata.in/knowledge-base/article/historical-data-availability-through-rest-api)
- [TrueData request limits](https://feedback.truedata.in/knowledge-base/article/historical-real-time-data-availability-through-market-data-api)
- [NSE Data & Analytics — data vending](https://www.nseindia.com/static/nse-data-and-analytics/data-information-vending)
- [NSE paid real-time data](https://www.nseindia.com/static/market-data/real-time-data-subscription)
- [Global Datafeeds authorised vendor page](https://globaldatafeeds.in/authorised-data-vendors/)
- [Global Datafeeds about / authorization](https://globaldatafeeds.in/about-us/)
- [Global Datafeeds data availability](https://globaldatafeeds.in/global-datafeeds-apis/global-datafeeds-apis/introduction/type-of-data-available/)
- [Global Datafeeds GetHistory](https://globaldatafeeds.in/global-datafeeds-apis/global-datafeeds-apis/rest-api-documentation/function-gethistory/)
- [Global Datafeeds terms](https://globaldatafeeds.in/terms-and-conditions/)
- [Kite Connect terms](https://kite.trade/terms/)
- [Kite Connect FAQs](https://support.zerodha.com/category/trading-and-markets/general-kite/kite-api/articles/kite-connect-api-faqs)

Open questions for sales/legal: [integration-truedata-questions.md](integration-truedata-questions.md) and [integration-globaldatafeeds-questions.md](integration-globaldatafeeds-questions.md).

---

## 1. Current AlphaLens state

Existing interface (`backend/.../integration/market/MarketDataProvider.java`):

```text
sourceName()
quote(instrumentId)
historical(instrumentId, timeframe, from, to)
```

Existing domain models already match Phase 28 needs:

- `Quote`: instrumentId, lastPrice, change, OHLC, previousClose, volume, asOf, source, market
- `Candle` / `HistoricalData`
- `Timeframe`: 1M, 5M, 15M, 1H, 1D, 1W

What is **not** on the interface yet (Phase 28+):

- `subscribeQuotes` / WebSocket listener
- stock identification mapping (vendor symbol ↔ internal `instrumentId`)
- market-status
- corporate-action feed
- source / retrievedAt / dataVersion on persisted candles

The rest of the app already depends on `MarketDataProvider`, not a vendor class. Phase 28 must keep that.

Frontend never calls a vendor. Charts use backend candles only (`lightweight-charts`).

---

## 2. Product requirements (must satisfy)

### Real-time

- NSE and BSE cash equities (indices later)
- LTP, OHLC, volume
- WebSocket
- Scalable subscriptions for an initial 100–300 stock universe

### Historical

- Daily, 1-minute, 5-minute, 15-minute (30-minute / 1-hour where available)
- 5+ years daily
- Enough intraday history for research (default vendor APIs are **much shorter** — see below)

### Corporate actions

- Splits, bonus, rights, dividends, mergers, symbol changes, ISIN changes
- Ability to keep **raw** and **adjusted** series (Phase 31)

### Commercial

Must confirm in writing before Phase 28:

- commercial use
- display to end users on a website/app
- caching and historical storage on our servers
- derived analytics (scores, algos, indicators)
- user-count / device / website-display fees
- WebSocket redistribution (browser must **not** connect to the vendor; we may still need a display license)
- paper trading / simulated portfolios (several vendors and NSE circulars restrict gaming / virtual trading)

---

## 3. Rejected as production market-data sources

| Source | Why rejected |
| --- | --- |
| **Zerodha Kite Connect** | Official FAQ: displaying or redistributing Kite data on external platforms violates exchange data-vending policy. Kite is an order/portfolio API, not a data vendor. Terms forbid public display of live market data and permanent copies for redistribution. Keep `BrokerProvider` only (Phases 38–44). |
| **NSE/BSE website scrape** | Not a licensed feed. Unstable, against exchange terms, unusable for commercial display. |
| **Yahoo / Google / unpaid public APIs** | Not exchange-authorised for Indian commercial redistribution. Historical lists show some firms as vendors; that does not license scraping their consumer sites. |
| **Direct NSE multicast / leased line (now)** | Official path via NSE Data & Analytics. Requires POP leased line, exchange contract, and ops we do not have. Valid later for institutional scale; not the first adapter. |

---

## 4. Candidate comparison

Pricing is **not published** for commercial/display use. Both API vendors quote by exchange segment, symbol count, and real-time vs historical. Exchange fees are extra. Treat all INR figures as unknown until a written quote.

### A. TrueData Financial Information Pvt Ltd

| Topic | Official position |
| --- | --- |
| Provider | Authorised data vendor for NSE, BSE, MCX |
| Pricing | Custom. Depends on exchanges, symbol count, streaming vs charting vs corporate. Exchange fees separate. Contact `contact@truedata.in` / product form. |
| Data coverage | NSE EQ, indices, F&O, CDS; BSE EQ, indices, F&O; MCX. Knowledge-base note (getting started): at that time RT + history via API for NSE EQ/indices/F&O/CDS and MCX — **confirm current BSE API coverage in writing**. |
| Latency | Marketed as ultra-low / millisecond; Level 1 (best bid/ask default). |
| API | REST + WebSocket. Java compatible (no first-party Java SDK advertised; Python / Node / .NET official). JSON default. |
| WebSocket | Tick, 1-minute, and 5-minute streaming. Subscribe symbols; no per-tick REST loop. |
| Historical | Official default: ticks = last 5 trading days; 1–60 min bars = last **6 months**; daily = **10+ years**. Extended history mentioned as a future/add-on — **must confirm**. |
| Rate limits (history REST) | Official: ticks 5/sec, 300/min; minute bars 10/sec, 600/min; 18,000/hour both. Real-time stream: no such REST-style limit once subscribed. |
| Corporate actions | Corporate Data API lists announcements, results, shareholding, **corporate actions (dividends, splits, rights, mergers)**, news. |
| Commercial license | Data is licensed, not owned. Personal/internal use only by default. **Display on a website/app or commercial platform requires written approval from the exchanges and TrueData.** Redistribution is a separate license. |
| Limitations | Approved intervals only: tick, 1m, 2m, 5m, 15m **delayed**, EOD. TrueData states it does **not** provide data for gaming / virtual / simulation use unless NSE & SEBI approvals exist. Educational use of **real-time** feeds is prohibited; delayed (min one-day) may be allowed after exchange license. |

**Fit:** Strong API surface for AlphaLens (quotes, charts, corporate actions, Java REST). Weak default **intraday history** for Phase 34–36. Commercial display is **not** included in a personal API plan.

### B. Global Financial Datafeeds LLP (Global Datafeeds)

| Topic | Official position |
| --- | --- |
| Provider | Authorised realtime L1 vendor of NSE Data & Analytics, BSE, MCX, NCDEX |
| Pricing | Not published for API/display. Contact sales. |
| Data coverage | NSE CM/FO/CDS, BSE CM/FO, MCX, NCDEX (NCDEX listed as 5-min delayed on about page). Fundamentals / news / widgets sold separately. |
| Latency | L1 realtime ~1 second snapshots on WebSocket `SubscribeRealtime`. |
| API | WebSockets, REST, .NET, COM, FIX. Documented `GetHistory`, `SubscribeRealtime`, `SubscribeSnapshot`, `GetLimitation`. One active WebSocket session per API key. |
| WebSocket | Authenticate with API key, then subscribe. JSON only. |
| Historical | Official availability table (BSE cash example): tick = 1 calendar week; 1–4 min = **3 months**; 5–12 min = **4.5 months**; 15–30 min / hours = **6 months**; day/week/month **since 2007**. Support policy also says EOD since 2010 if a symbol is re-added. `AdjustSplits` on GetHistory (default true). |
| Corporate actions | Split adjustment flag on history. Separate fundamental/corporate products exist; **do not assume** full Phase 31 action types on the market-data key. |
| Commercial license | Terms: realtime / IEOD data is solely for subscribers and **may not be redistributed** in any form without **explicit written permission**. |
| Limitations | Symbol / function / history limits are account-specific (`GetLimitation`). Session exclusivity on WebSocket. |

**Fit:** Clear REST/WS docs and `AdjustSplits` help Phase 29–31. Intraday history is still months, not years. Commercial display still needs a written addendum.

### C. NSE Data & Analytics (direct)

| Topic | Official position |
| --- | --- |
| Provider | Exchange info-vending arm (formerly DotEx) |
| Pricing | Published domestic/international tariffs (effective 1 Apr 2026 on the vending page). Snapshot/delayed commercials are **per display medium** (website vs app). |
| Data coverage | CM, F&O, CDS, WDM, commodity, corporate data, corporate bonds |
| Latency | Realtime: multicast over customer-owned leased line from NSE POP, or via authorised vendor. Also 1/5-min snapshot (SFTP) and 15-min delayed snapshot (FTP). |
| API | Not a public developer REST/WebSocket portal. Binary/multicast/SFTP. |
| Historical / corporate | Separate paid products (corporate data tariffs are listed on NSE pages). |
| Commercial license | This **is** the licensing source of truth. Vendors sublicense under these rules. |

**Fit:** Correct for a later institutional feed. Too heavy for Phase 28. We still need an NSE/BSE **display/storage** conversation even if we buy through a vendor.

### D. Institutional terminals (Bloomberg, LSEG/Refinitiv, FactSet, Cogencis)

Authorised on NSE vendor lists. Excellent data quality and entitlements process. Cost and contract complexity are typically far above an early 100–300 name research product. Keep as a fallback if TrueData/Global Datafeeds cannot license website display.

---

## 5. Historical-depth gap (blocks naive Phase 29–36)

Phase requirements ask for “sufficient intraday history for research.” Official **default** API backfill:

| Timeframe | TrueData default | Global Datafeeds default |
| --- | --- | --- |
| 1-minute | 6 months | 3 months |
| 5-minute | 6 months | 4.5 months |
| 15-minute | 6 months | 6 months |
| Daily | 10+ years | since 2007 / 2010 |

Neither default API gives multi-year 1-minute history. AlphaLens must:

1. Ask both vendors for **extended historical** / bulk backfill products, and
2. Plan Phase 29 to **accumulate** intraday bars in TimescaleDB going forward, and
3. Run long-horizon research primarily on **daily** series until extended intraday is licensed.

Do not invent extra history.

---

## 6. Decision

**Preferred production vendor (intent): TrueData.**

Reasons:

1. Exchange-authorised for NSE and BSE (required).
2. REST + WebSocket that map cleanly onto the existing `MarketDataProvider` without leaking vendor types into services.
3. Corporate Data API explicitly includes corporate actions needed for Phase 31.
4. Official Java compatibility via REST/WS (our backend is Spring Boot).
5. Daily history (10+ years) meets the 5-year daily gate.

**Runner-up: Global Datafeeds.**

Use if TrueData cannot license website display, BSE streaming, or extended history on acceptable terms. Their `GetHistory` + `AdjustSplits` and longer published EOD history are advantages.

**Not selected for market data: Kite.**

Kite remains `BrokerProvider` only.

**Implementation status: not approved.**

Standard personal/API subscriptions are **internal/single-subscriber** licenses. AlphaLens will display quotes and charts to end users, cache candles, compute derived scores, and later run paper portfolios. That is redistribution/display until a vendor + exchange says otherwise in writing.

---

## 7. Integration architecture (when Phase 28 is approved)

```text
QuoteService / HistoricalDataService / MarketStatusService
        ↓
MarketDataProvider
        ↓
TrueDataMarketDataAdapter   (or GlobalDatafeeds adapter)
        ↓
vendor REST / WebSocket
```

Never:

```text
StockService → TrueData client
Vue → vendor WebSocket
```

Credentials (placeholders only in `.env.example` later):

```text
MARKET_DATA_PROVIDER=MOCK|TRUEDATA|GLOBALDATAFEEDS
MARKET_DATA_API_KEY=
MARKET_DATA_API_SECRET=
MARKET_DATA_BASE_URL=
MARKET_DATA_WS_URL=
```

Secrets stay server-side. Health: `GET /internal/integrations/market-data/health` in Phase 28.

---

## 8. Risks

1. **Unlicensed display** — highest legal risk. Do not ship live LTP/charts to users on a personal vendor plan.
2. **Paper trading / virtual portfolio** — TrueData and NSE circulars restrict gaming/virtual trading on exchange data. Phase 43 may need delayed data or a separate approval.
3. **Intraday history too short** for walk-forward research without an add-on or self-accumulation.
4. **15-minute live bars** may be delayed-only at TrueData.
5. **BSE coverage** on TrueData’s API knowledge-base notes lags the marketing site — confirm before buying.
6. **Symbol mapping** — vendors use their own identifiers; stock identification remains AlphaLens `instrumentId` + ISIN.
7. **Pricing unknown** — website-display + per-user exchange fees can dominate infra cost.
8. **No Java first-party SDK** — we write our own adapter (correct, avoids lock-in).

---

## 9. Testing plan (Phase 27)

- No production code change.
- Decision and question lists reviewed against official pages listed above.
- Existing mock provider and tests remain the runtime path.

Phase 28 tests (after contract): adapter unit tests with recorded fixtures only; one controlled sandbox call if the vendor issues a trial that covers our use case.

---

## 10. Gate to Phase 28

Phase 28 may start only when **all** of the following are true:

1. Written quote and contract naming AlphaLens as a commercial display/storage user.
2. Answers recorded in `docs/integration-truedata-questions.md` (or the GFDL file if that vendor wins).
3. `docs/data-licensing.md` updated with vendor name, contract date, and permitted uses.
4. `docs/integrations.md` status set to Active for that vendor.
5. Credentials exist only in local/secret manager — never committed.

Until then the selected provider is **TrueData (pending license)** and the running provider stays **MOCK**.
