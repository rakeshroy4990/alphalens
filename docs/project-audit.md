# PHASE 0 — Project Audit

**Project:** AlphaLens  
**Repository:** `/Users/rakeshroy/Documents/Projects/alphalens`  
**Audit date:** 2026-09-13  
**Reference architecture:** `/Users/rakeshroy/Documents/Projects/saas-builder-project`  
**Scope:** Phase 0 inspected an empty repo. Phase 1 scaffolding was added after approval.

---

## 1. Current project structure

The repository is a newly initialized Git repo on `main` with **no commits**.

```text
alphalens/
├── .git/
├── .cursor/rules/          # installed this audit (agent guidance)
│   ├── alphalens-master.mdc
│   ├── alphalens-integrations.mdc
│   ├── alphalens-domain.mdc
│   └── alphalens-phases.mdc
└── docs/
    ├── master-development-rule.md
    └── project-audit.md      # this file
```

There is no `frontend`, `backend`, `infrastructure`, or `scripts` directory.

---

## 2. Existing frontend

**None.**

No Vue app, Vite config, `package.json`, Pinia stores, router, or chart library.

saas-builder-project comparison (patterns only; not copied):

| Item | saas-builder-project | AlphaLens today |
| --- | --- | --- |
| UI framework | Vue 3 + Vite + TypeScript | Missing |
| State | Pinia | Missing |
| Routing | Vue Router | Missing |
| Styling | Tailwind CSS v4 (`@tailwindcss/vite`) | Missing |
| HTTP | Axios behind services (not in components) | Missing |
| Tests | Vitest + Vue Test Utils | Missing |
| Charts | Chart.js in hospital UI | Missing (Lightweight Charts is the target) |
| Extra | Declarative/config-driven UI, i18n, Firebase | Out of scope for AlphaLens Phase 1 |

**Recommendation:** Do **not** reuse the hospital declarative UI framework. AlphaLens needs a conventional Vue research UI (stock page, charts, screeners) with business logic in stores/services/composables.

---

## 3. Existing backend

**None.**

No Java sources, Gradle wrapper, Spring Boot application, controllers, or health endpoint.

saas-builder-project comparison:

| Item | saas-builder-project | AlphaLens today |
| --- | --- | --- |
| Language / build | Java 17, Gradle | Missing |
| Framework | Spring Boot 3.4.2 | Missing |
| Layers | Controller → Service → Repository | Missing |
| Security | Spring Security + JWT (`backend-auth-lib`) | Missing |
| Validation | `spring-boot-starter-validation` | Missing |
| Migrations | Flyway (`backend-hospital`) | Missing |
| Persistence | JPA + PostgreSQL (hospital); Mongo in older modules | Missing |

The generic `backend` module in saas-builder is a thin FlexShell/Mongo UI-metadata service. The closer architectural match is `backend-hospital` (Spring Web, Security, JPA, Flyway, validation) — **conventions only**, not hospital domain code.

---

## 4. Existing database

**None.**

No PostgreSQL schema, TimescaleDB hypertables, Flyway/Liquibase migrations, Redis config, or seed data.

saas-builder-project hospital uses PostgreSQL + Flyway (`V1__baseline_postgres.sql` … `V38__…`) with `ddl-auto=none`. That migration **tooling** should be reused as a pattern. The hospital tables must not be copied.

---

## 5. Existing authentication

**None.**

No users table, email/password flow, Google OAuth, JWT issuance, or session cookies.

saas-builder-project has a mature JWT + httpOnly cookie + Google OAuth path in `backend-auth-lib` and hospital `application.properties`. AlphaLens should follow the same **security shape** (server-side tokens, no secrets in Vue) but implement auth in Phase 1+ as a first-party module, not by importing the hospital auth library (it is coupled to FlexShell packaging and hospital concerns).

---

## 6. Existing dependencies

**None.** No `package.json`, `build.gradle`, lockfiles, or wrapper jars.

Intended first dependencies (Phase 1, not installed):

**Frontend:** `vue`, `vue-router`, `pinia`, `vite`, `@vitejs/plugin-vue`, `typescript`, `tailwindcss`, `@tailwindcss/vite`, `axios`, `vitest`, `@vue/test-utils`. Chart library waits until Phase 7 unless a stub dependency is required to compile — prefer adding Lightweight Charts only in Phase 7.

**Backend:** Spring Boot Web, Validation, Security, Data JPA, Flyway, PostgreSQL driver, Test starter. Redis, Timescale-specific bits, OpenAI, Kite, and S3 wait for their phases.

---

## 7. Existing tests

**None.** No Gradle tests, Vitest, or API tests.

---

## 8. Existing environment configuration

**None.** No `.env`, `.env.example`, `application.properties`, or Docker Compose.

saas-builder-project loads `backend-*/.env` via Gradle `bootRun` and keeps secrets out of Git. Same approach should be used in Phase 1.

---

## 9. Existing Docker / infrastructure

**None.**

saas-builder-project has Cloud Build, Render, and a Coturn compose file. It does **not** have a simple local Postgres/Redis compose at repo root. AlphaLens Phase 1 should add a focused local `infrastructure` compose (PostgreSQL; Redis later or as a stub) rather than copying Cloud Build/hospital deploy YAML.

---

## 10. Build system

**None** in this repo.

Reference: Gradle (Spring Boot plugin 3.4.2) + npm/Vite 8 in saas-builder-project. AlphaLens should use the same pair: `backend/gradlew` and `frontend` npm scripts (`dev`, `build`, `test`).

---

## 11. What can be reused (from saas-builder-project)

Reuse **conventions and versions**, not product code:

* Vue 3 + Vite + Pinia + Vue Router + Tailwind v4 + TypeScript + Vitest
* Spring Boot 3.4.x, Java 17, Gradle wrapper, Flyway, `ddl-auto=none`
* Layering: controllers stay thin; DTOs not JPA entities on the wire
* Frontend: `<script setup>`, composables, services wrap HTTP, no credentials in the client
* JWT/cookie auth ideas and Google OAuth as a later-compatible design
* `.env` + `.env.example` + gitignore for secrets
* Health endpoint + CORS allowlist pattern
* S3-compatible storage behind an interface (hospital uses AWS SDK)

Do **not** reuse:

* Hospital/ecommerce/social domain models
* Declarative UI metadata / DynamicPage renderer
* MongoDB persistence
* PascalCase JSON wire format (hospital-specific; AlphaLens should use conventional camelCase JSON unless a later API contract says otherwise)
* Agora/WebRTC, BLE, Firebase hosting, i18n packages, extensibility libs
* Kite-as-market-data (forbidden by product rule even if a broker lib is added later)

---

## 12. What needs to be created

Everything in Phase 1 and beyond, including:

* `/frontend`, `/backend`, `/infrastructure`, `/scripts` (docs already started)
* Gradle + Spring Boot app with `/api/health` (or equivalent)
* Vue + Vite app that can call the backend
* `.gitignore`, `.env.example`, Docker Compose for local Postgres
* README and local development instructions
* Later phases: instrument master, mock market data, TimescaleDB, charts, analytics, algo engine, Kite, etc.

---

## Current architecture

Greenfield monorepo intended to follow saas-builder-project’s Vue + Spring Boot layout, specialized for stock intelligence. **No runtime architecture exists yet.** Only process artifacts (rules + this audit) are present.

```text
[empty]  →  (planned) ingestion → PostgreSQL + TimescaleDB → Redis
         → domain services → REST → Vue research UI + future Kite adapter
```

---

## Existing components

| Area | Status |
| --- | --- |
| Git repository | Initialized, no commits |
| Cursor rules | Installed (master, integrations, domain, phases) |
| Product rule document | `docs/master-development-rule.md` |
| Frontend / backend / DB / auth / cache / Docker / tests / CI | Absent |

---

## Missing components

* Runnable frontend and backend
* PostgreSQL + migration tooling
* TimescaleDB, Redis, S3 adapter
* Authentication
* Instrument model and APIs
* Market-data provider (mock first)
* Chart API and Lightweight Charts UI
* Fundamentals, valuation, algo, scores
* Watchlists, alerts, screener, AI, portfolio, Kite
* Data-licensing decision (`docs/data-licensing.md` — required before any production market-data provider)
* Production observability and hardening

---

## Recommended architecture

Keep the master-rule architecture. Align implementation details with saas-builder-project:

```text
alphalens/
  frontend/          Vue 3 + Vite + TS + Pinia + Router + Tailwind
  backend/           Spring Boot 3.4 + Java 17 + Gradle + Flyway
  docs/              rules, API, licensing
  infrastructure/    local Docker (Postgres now; Timescale/Redis when those phases start)
  scripts/           start-dev and helper scripts
```

Backend package layout (Phase 1): `controller`, `service`, `repository`, `domain`, `dto`, `mapper`, `config`, `exception`, `integration`.

Frontend layout (Phase 1): `components`, `views`, `stores`, `services`, `router`, `types`, `composables`, `layouts`.

Providers remain interfaces from day one even if only a mock `MarketDataProvider` appears in Phase 4.

Package name suggestion (Phase 1 decision): `com.alphalens` (not `com.flexshell`).

---

## Risks

1. **Market-data licensing.** Redistributing NSE/BSE data commercially is legally constrained. Phase 4 must stay on a mock provider until `docs/data-licensing.md` records a licensed source. Do not use Kite as the market-data backbone.
2. **Copying saas-builder too literally.** Hospital’s declarative UI, Mongo leftovers, and PascalCase APIs would slow AlphaLens and fight the stock-page UX.
3. **Phase creep.** The product vision is large (26 phases). Implementing charts, Kite, or OpenAI before their phases will create secret leaks and licensing risk.
4. **Financial integrity.** Dummy or interpolated numbers in early UI demos can become “real” in users’ minds. Mock data must be labeled as mock.
5. **Auth library coupling.** Importing `backend-auth-lib` pulls FlexShell/hospital assumptions. Prefer a small first-party auth module.
6. **Timescale vs plain Postgres.** Adding Timescale too early (before Phase 5) complicates local setup. Phase 1–2 should use PostgreSQL only.
7. **Regulatory positioning.** Scores and algorithms must stay framed as research/education until a compliance review.

---

## Next phase

**PHASE 1 — Repository Foundation**

Create `/frontend`, `/backend`, `/infrastructure`, `/scripts`; add `.env.example`, `.gitignore`, Docker, README; get frontend and backend running; connect the database; expose a health endpoint; prove the frontend can call the backend.

Do not start Phase 1 until explicitly approved.

---

## Audit checklist (Phase 0)

| # | Item | Finding |
| --- | --- | --- |
| 1 | Repository | Empty Git repo + docs/rules only |
| 2 | Frontend | Missing |
| 3 | Backend | Missing |
| 4 | Database | Missing |
| 5 | Authentication | Missing |
| 6 | Dependencies | None installed |
| 7 | Build system | None |
| 8 | Environment configuration | Missing |
| 9 | Tests | Missing |
| 10 | Docker | Missing |
| 11 | Reuse | saas-builder Vue/Spring/Flyway conventions only |
| 12 | Recommended next phase | PHASE 1 — Repository Foundation |
