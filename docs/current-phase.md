# Current phase

**PHASE 27 COMPLETE** — Market data provider selection (documentation only).

Phases 0–26 remain a working research slice on **MOCK** data. No production vendor adapter was added.

Phase 27 deliverables:

- [docs/market-data-provider-decision.md](market-data-provider-decision.md)
- [docs/integrations.md](integrations.md)
- [docs/integration-truedata-questions.md](integration-truedata-questions.md)
- [docs/integration-globaldatafeeds-questions.md](integration-globaldatafeeds-questions.md)

Preferred vendor (pending written license): **TrueData**. Runner-up: **Global Datafeeds**. Kite stays broker-only.

**PHASE 28 is blocked** until commercial display/storage answers are recorded and `docs/data-licensing.md` names a signed vendor.

Google Cloud packaging (not a product phase): all-in-one image in `infrastructure/gcp/Dockerfile`. See [gcp-deploy.md](gcp-deploy.md).

Known limitations: live NSE/BSE data, OpenAI, and Kite sessions are not connected. Redis is not required locally. Auth is first-party JWT + httpOnly cookies via a **login/register popup** (email/password and optional Google GIS access token). Production must set `APP_AUTH_JWT_SECRET`.

This is research and education software, not investment advice.
