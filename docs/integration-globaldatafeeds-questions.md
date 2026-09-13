# Global Datafeeds — questions before switching preferred vendor

Use if TrueData cannot license AlphaLens, or as a parallel quote. Official terms already say realtime/IEOD data **must not be redistributed** without explicit written permission.

## Commercial and legal

1. Written permission to display quotes/charts to end users on a commercial website and app?
2. Permission to persist history in our database and serve it through our API (users never call GFDL)?
3. Derived analytics (indicators, scores, algos) — allowed to show to users?
4. Pricing for 100–300 NSE+BSE cash names, realtime WebSocket + REST history?
5. Per-user / per-website exchange display fees?
6. Paper trading / virtual portfolio restrictions?
7. One WebSocket session per API key — what is the multi-instance / HA model for production?

## Data and API

8. Confirm history table for **NSE cash** (not only BSE): 1-minute depth, 5-minute, 15-minute, daily start year.
9. Extended / bulk historical beyond the published 3–6 month intraday windows?
10. `AdjustSplits` coverage: bonus, rights, dividends, mergers, symbol/ISIN changes?
11. Separate corporate-action / fundamental API entitlements and keys?
12. ISIN-based instrument mapping?
13. Official REST base URL, auth (`accessKey`), rate limits, and `GetLimitation` for a commercial key?
14. Trial that matches commercial display, not only desktop charting plugins?

Record written answers here before any `GlobalDatafeedsMarketDataAdapter` work.
