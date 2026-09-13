# Third-party integration registry

Update this table whenever an integration is added or its contract changes.

Core rule: application code depends on **internal ports** (`MarketDataProvider`, `BrokerProvider`, `AIProvider`, `NotificationPort`). Vendor types stay in `integration.*` adapters.

| Integration | Purpose | Environment | Status | Credentials | Commercial rights |
| --- | --- | --- | --- | --- | --- |
| Market data — TrueData (preferred) | Historical + realtime NSE/BSE quotes and candles | Production | Pending license | Server (`MARKET_DATA_*`) | Must verify — see [market-data-provider-decision.md](market-data-provider-decision.md) |
| Market data — Global Datafeeds (runner-up) | Alternate authorised vendor | Production | Pending | Server | Must verify — [integration-globaldatafeeds-questions.md](integration-globaldatafeeds-questions.md) |
| Market data — Mock | Deterministic quotes/candles labeled `MOCK` | Local / CI | Active | None | Internal only |
| TradingView Lightweight Charts 4.2.3 | Chart rendering only (no vendor data) | Frontend | Active | None | Apache 2.0; keep TradingView attribution ([NOTICE](https://github.com/tradingview/lightweight-charts) + link to tradingview.com / `attributionLogo`) |
| OpenAI | AI explanations of **already calculated** structured inputs | Production | Pending (adapter is `MockAIProvider`) | Server | Active API terms once a key is issued; AI must not calculate metrics |
| Zerodha Kite Connect | Broker: login, holdings, positions, orders | Production | Pending (`KiteBrokerProvider` stub) | Server (`KITE_API_KEY`, `KITE_API_SECRET`, encrypted access token) | Must verify commercial / Rainmatter path; **not** a market-data license |
| Email / notification provider | Alert delivery | Production | Pending (`NotificationPort` only) | Server | Verify before Phase 16 production send |
| Payment provider (Razorpay / Stripe) | Subscriptions | Production | Pending | Server | Phase 47 — wrap in `BillingProvider` |
| Product analytics | Usage analytics | Production | Pending | Client/Server | Verify DPDP + no PII leakage; Phase 45+ |
| Firebase Hosting | Static Vue frontend | Production | Active (`alphalens-a3cce`) | CLI (`firebase login`) | Confirm Firebase/Google Cloud billing |
| Google Cloud | Artifact Registry, Cloud Build, Cloud Run, Cloud SQL | Production | Active (`alphalens-508509`) | CLI (`gcloud auth`) | Confirm billing and IAM |

## Integration protocol (every new vendor)

1. Identify requirement (why, MVP?, build vs buy).
2. Read **official** documentation only.
3. Check commercial licensing. If unclear → stop and write `docs/integration-<provider>-questions.md`.
4. Classify credentials (public vs server vs OAuth). Secrets never in Vue.
5. Implement adapter behind the existing port.
6. Environment variables + `.env.example` placeholders.
7. Health check.
8. Timeouts, limited retry, backoff; circuit breaker where appropriate.
9. Respect provider rate limits.
10. Log provider, operation, duration, status, error category, correlation id — never secrets or auth headers.
11. Tests: mocks first; live calls rare and gated.
12. Update this file and the provider decision/questions doc.

## Do not integrate yet

- Production market-data WebSocket (Phase 32) until Phase 27 license is signed.
- Browser → vendor sockets.
- Live OpenAI until Phase 20 production key + policy review (mock already exists).
- Live Kite until Phases 38–39.
- Payments until Phase 47.

## Related docs

- [market-data-provider-decision.md](market-data-provider-decision.md)
- [data-licensing.md](data-licensing.md)
- [integration-truedata-questions.md](integration-truedata-questions.md)
- [integration-globaldatafeeds-questions.md](integration-globaldatafeeds-questions.md)
