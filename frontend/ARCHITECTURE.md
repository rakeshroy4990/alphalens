# Frontend Architecture Map

AlphaLens follows the same chrome and layering as `frontend-hospital/ARCHITECTURE.md`, without copying the hospital FlexShell renderer or hospital domain pages.

## 1) High-level flow

1. User lands on a Vue Router path (`/`, `/screener`, `/stocks/:instrumentId`, …).
2. `App.vue` mounts the layout.
3. `AppLayout` renders shared **header**, **page body**, and **footer**.
4. The home page adds a **hero** plus section cards.
5. Views call stores. Stores call services. Services are the only HTTP boundary.

## 2) Layer map

- **Presentation entry** — `src/router/`, `src/App.vue`, `src/layouts/AppLayout.vue`
- **Global system UI** — `src/components/system/` (`SiteHeader`, `SiteFooter`)
- **Dumb/pure UI** — `src/components/primitives/` (`HeroSection`, `SectionCard`)
- **Page views** — `src/views/`
- **Page/chrome config** — `src/configs/siteChrome.ts`
- **Service/API** — `src/services/`
- **State** — `src/stores/`

## 3) Home page composition

Same order as the hospital public home:

1. Sticky header (brand, nav, primary CTA, mobile menu)
2. Hero (title, subtitle, CTAs, stats, search)
3. Section cards (instrument master, method)
4. Footer (research disclaimer)

**Do not show System status** on any research page.

## 4) Core principles

- No business logic in Vue templates.
- Services are the only API boundary.
- Shared chrome is config-driven (`siteChrome`, `homeContent`).
- Do not put secrets in the frontend.
