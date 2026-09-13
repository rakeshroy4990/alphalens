# Research APIs

All JSON is camelCase. Instruments are identified by internal `instrumentId`, never by NSE symbol.

Quotes, candles, fundamentals, and scores are **MOCK** unless a licensed provider is configured. See [data-licensing.md](../data-licensing.md).

| Method | Path | Notes |
| --- | --- | --- |
| GET | `/api/instruments?page&size` | Paginated master |
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
| POST | `/api/screener` | Filters |
| GET/POST | `/api/watchlists` | Multiple lists |
| GET/POST | `/api/alerts` | Alert definitions |
| GET/POST | `/api/coverage-requests` | Coverage queue |
| GET/POST/DELETE | `/api/user-algorithms` | JSON rules, no user code |
| GET/POST | `/api/portfolio` | Manual / CSV |
| GET/POST | `/api/broker/*` | Kite adapter; tokens stay on the server |
| POST | `/api/orders/preview` + `/confirm` | Confirmation required |
| POST | `/api/backtests` | Simulated results, bias warnings |

Timeframes: `1M`, `5M`, `15M`, `1H`, `1D`, `1W`.
