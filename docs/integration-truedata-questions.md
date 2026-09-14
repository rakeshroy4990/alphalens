# TrueData — questions before Phase 28

Send to TrueData sales (`contact@truedata.in` / Market Data API form) and keep written answers here. Do not implement a production adapter until these are answered.

Product to describe: AlphaLens, a commercial Indian stock-research website/app. We will:

- show LTP, OHLC, volume, and charts to **end users**
- cache and store history on our PostgreSQL/TimescaleDB
- compute derived scores, indicators, and algorithms
- **not** connect browsers to TrueData
- later add paper portfolios and (after compliance) user-confirmed broker orders

## Commercial and legal

1. Is website/app **display to paying and free end users** allowed? Which NSE/BSE display license is required, and who files it (us vs TrueData)?
2. Is **redistribution** of ticks or candles to users (via our API/WebSocket) allowed if users never see TrueData credentials?
3. May we **store historical** OHLCV indefinitely for charts and research?
4. May we store and display **derived** data (SMA, RSI, scores, algo signals) to users?
5. How are fees calculated: symbols, unique users, devices, websites, apps, delayed vs realtime?
6. What is the quote for an initial **100–300 NSE+BSE cash** universe, realtime + daily history + 1/5/15-minute bars?
7. Are exchange fees billed through TrueData or separately to NSE Data & Analytics / BSE?
8. Paper trading / virtual portfolios: TrueData’s public page prohibits gaming/virtual/simulation without NSE & SEBI approval. What data (delayed EOD?) can we use for Phase 43?
9. Educational / research positioning: may we show **realtime** quotes, or only delayed (minimum one-day) until we have a specific license?
10. Attribution and audit-log requirements?

## Data and API

11. Confirm **BSE cash realtime + history** on the Market Data API (knowledge-base notes have lagged the marketing site).
12. Confirm default history: ticks 5 days, minute bars 6 months, daily 10+ years — and price/availability of **extended intraday history** (multi-year 1-minute).
13. Are **live 15-minute** bars allowed, or only delayed 15-minute as the public interval list states?
14. Corporate Data API: which action types (SPLIT, BONUS, RIGHTS, DIVIDEND, MERGER, DEMERGER, SYMBOL_CHANGE, ISIN_CHANGE) and how are they keyed (ISIN vs symbol)?
15. Raw vs split-adjusted series — can we request both?
16. Stock identification / ISIN mapping API (vendor instrument master) and change files?
17. Market-status / holiday calendar API?
18. WebSocket connection limits, max concurrent symbols, reconnect rules, heartbeat?
19. Sandbox that matches **display** entitlements (not only personal algo use)?
20. Java/REST official endpoint list, auth scheme (API key vs user/password), and IP allowlisting?
21. SLA / uptime credits and incident contact?
22. Trial terms if we describe commercial display (not personal charting)?

## Stop conditions

If TrueData cannot license display + storage, do not code `TrueDataMarketDataAdapter`. Evaluate Global Datafeeds using [integration-globaldatafeeds-questions.md](integration-globaldatafeeds-questions.md).
