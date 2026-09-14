# Research APIs

All JSON is camelCase. Each stock is identified by internal `instrumentId` (stock identification), never by NSE symbol. Java/API still use the `Instrument` type and `/api/instruments` paths.

Quotes, candles, fundamentals, and scores are **MOCK** unless a licensed provider is configured. See [data-licensing.md](../data-licensing.md).

| Method | Path | Notes |
| --- | --- | --- |
| GET | `/api/instruments?page&size` | Paginated stock identification |
| GET | `/api/instruments/search?q=` | Name, symbol, ISIN |
| GET | `/api/instruments/{id}` | Detail |
| GET | `/api/instruments/{id}/quote` | Mock quote |
| GET | `/api/instruments/{id}/candles?timeframe&from&to` | OHLCV, max bars enforced |
| GET | `/api/instruments/{id}/indicators` | SMA EMA RSI MACD VWAP Bollinger |
| GET | `/api/instruments/{id}/fundamentals` | Statements |
| GET | `/api/instruments/{id}/analytics` | CAGR, margins, trends |
| GET | `/api/instruments/{id}/valuation` | PE/PB/DCF + assumptions |
| GET | `/api/instruments/{id}/score` | Component scores + weights |
| GET | `/api/instruments/{id}/algorithm` | Default configurable rules |
| GET | `/api/instruments/{id}/page` | Stock page aggregate |
| GET | `/api/research/{id}/explain` | AI explains structured inputs only |
| POST | `/api/screener` | Filters; computes analytics/valuation/score once per listed stock |
| GET/POST | `/api/watchlists` | Multiple lists; **authenticated** |
| GET/POST | `/api/alerts` | Alert definitions; **authenticated** |
| GET/POST | `/api/coverage-requests` | Coverage queue; **authenticated** |
| GET/POST/DELETE | `/api/user-algorithms` | JSON rules, no user code; **authenticated** |
| GET/POST | `/api/portfolio` | Manual / CSV; **authenticated** |
| GET/POST | `/api/broker/*` | Kite adapter; tokens stay on the server; **authenticated** |
| POST | `/api/orders/preview` + `/confirm` | Confirmation required; **authenticated** |
| POST | `/api/backtests` | Simulated results, bias warnings; **authenticated** |

Auth: [auth.md](auth.md). Timeframes: `1M`, `5M`, `15M`, `1H`, `1D`, `1W`.
