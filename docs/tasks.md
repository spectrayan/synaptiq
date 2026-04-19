# Synaptiq MVP — Executable Task List

> Tasks are executed sequentially within each phase. Mark `[x]` when complete.
> Each task maps to requirements from `requirements.md`.

---

## Phase 0 — Foundation (Done ✅)
- [x] **T0.1** Initialize Nx 22 monorepo workspace
- [x] **T0.2** Scaffold Angular 21 shell app (zoneless, SSR, signals)
- [x] **T0.3** Scaffold FastAPI backend (modular monolith skeleton)
- [x] **T0.4** Add libs: `ui`, `chat`, `dsl-renderer`, `theme`, `types`, `utils`, `constants`, `angular-sdk`
- [x] **T0.5** Configure `tsconfig.base.json` with `@synaptiq/*` path aliases
- [x] **T0.6** Configure `nx.json` with standalone defaults
- [x] **T0.7** Add `@angular/material` + `@angular/cdk` to `package.json`
- [x] **T0.8** Create `docker-compose.yml` (MongoDB Atlas Local + Redis)
- [x] **T0.9** Write global M3 dark theme + design tokens in `styles.scss`
- [x] **T0.10** Implement chat shell layout (sidebar + topbar + message list + sticky input)
- [x] **T0.11** Save `requirements.md` + `architecture.md` to `docs/`
- [x] **T0.12** Initial git commit

---

## Phase 1 — Infrastructure & Auth

### 1A — Firebase Auth
- [ ] **T1.1** Enable Firebase Auth; configure Email/Password, Google OAuth, Microsoft OAuth `REQ-T6`
- [x] **T1.2** Add `firebase-admin` to FastAPI; initialize with service account `REQ-T6`
- [x] **T1.3** Implement `AuthMiddleware` — validate Firebase ID tokens on protected routes `REQ-NF14`
- [x] **T1.4** Add role custom claims: `platform_admin`, `tenant_admin`, `tenant_editor`, `tenant_viewer` `REQ-T8`
- [x] **T1.5** Angular `AuthService` (signals-based) — login, logout, token refresh `REQ-T6`
- [x] **T1.6** Angular `AuthGuard` for admin routes `REQ-T7`
- [x] **T1.7** Login page component (email/password + Google SSO) `REQ-T6`

### 1B — MongoDB Schema & Indexes
- [x] **T1.8** Define Pydantic models for all collections: `tenants`, `catalog_schemas`, `catalog_items`, `sessions`, `saved_items`, `action_logs`, `usage_ledger` `REQ-T2, REQ-D5`
- [x] **T1.9** Create Atlas indexes: tenant_id (all collections), item vector search, session_id `REQ-NF4`
- [x] **T1.10** Write MongoDB seed script (1 demo tenant + schema + 20 items)

### 1C — Tenant Middleware & Rate Limiting
- [x] **T1.11** Finalize `TenantMiddleware`: subdomain → `tenant_id` with Redis cache `REQ-T2, REQ-NF-CACHE1`
- [x] **T1.12** Redis-backed rate limiter: per-tenant 60 req/min; per-session 10 req/min `REQ-NF-RL1, REQ-NF-RL2`
- [x] **T1.13** Graceful in-chat rate limit message (not HTTP 429) `REQ-NF-RL3`
- [x] **T1.14** Log rate limit events to usage ledger `REQ-NF-RL4`

---

## Phase 2 — Tenant Management API

- [x] **T2.1** `POST /tenants` — create tenant (platform admin only) `REQ-T1, REQ-T3`
- [x] **T2.2** `GET/PATCH /tenants/{id}` — get/update tenant status + limits `REQ-T3, REQ-T5`
- [x] **T2.3** Auto-provision default config doc on tenant creation `REQ-T4`
- [x] **T2.4** `POST /tenants/{id}/admins` — invite admin via Firebase email `REQ-T8`
- [x] **T2.5** `GET /tenants/{id}/admins` — list admins with roles `REQ-T8`
- [x] **T2.6** Enforce per-tenant limits on all write operations `REQ-T5`

---

## Phase 3 — Catalog Schema (Import-First)

### 3A — Schema Import & Normalization
- [x] **T3.1** Define internal `CatalogSchema` model: fields array with type, label, searchable, filterable, sortable, displayable, visibility, designator (primary_label/image/price) `REQ-S1–S12`
- [x] **T3.2** `POST /catalog/schema/import` — accept OpenAPI spec (JSON/YAML), JSON Schema, or Synaptiq YAML; parse and normalize into `CatalogSchema` `REQ-S1–S3`
- [x] **T3.3** Schema parser: extract fields, infer types (string/number/boolean/date/enum/array/image_url), detect primary label/image/price heuristically `REQ-S4–S7`
- [x] **T3.4** Validate imported schema: 1–50 fields, one primary label, type enum enforcement `REQ-S4–S7`

### 3B — Schema CRUD & Lifecycle
- [x] **T3.5** `GET /catalog/schema` — retrieve active schema for tenant `REQ-S1`
- [x] **T3.6** `PATCH /catalog/schema` — annotate fields (toggle searchable/filterable/sortable, change designators, update labels, set visibility) `REQ-S3, REQ-S8, REQ-S9`
- [x] **T3.7** On schema save: invalidate tenant LLM system prompt cache + strip `admin_only` fields from end-user responses `REQ-S10–S12`

---

## Phase 4 — Catalog Data Management

### 4A — Item CRUD API
- [x] **T4.1** `POST /catalog/items` — create item (validated against schema) `REQ-D1, REQ-D8`
- [x] **T4.2** `GET /catalog/items` — paginated list with status filter
- [x] **T4.3** `GET/PATCH /catalog/items/{id}` — get/update item
- [x] **T4.4** `DELETE /catalog/items/{id}` — soft-delete (status → `archived`)
- [x] **T4.5** `PATCH /catalog/items/bulk` — bulk activate/deactivate/archive `REQ-D7`
- [x] **T4.6** Enforce max catalog item limit per tenant `REQ-D10`

### 4B — Import
- [x] **T4.7** `POST /catalog/import/csv` — upload, parse, validate per-row, import valid rows `REQ-D2, REQ-D8, REQ-D9`

> **Note:** Webhook import (T4.8), Google Sheets sync (T4.9–T4.10), and standalone admin UI (T4.11–T4.13) deferred to post-MVP. Admin catalog management handled via chat-based UI (Phase 7 DSL components + Phase 11 admin chat).

---

## Phase 5 — Vector Search & LLM System Prompt

- [x] **T5.1** Choose embedding model (Gemini `text-embedding-004` default; OpenAI `text-embedding-3-small` BYOK fallback)
- [x] **T5.2** On item create/update/delete: generate + store embedding in Atlas Vector Search `REQ-NF-CACHE2`
- [x] **T5.3** Background re-embedding job when schema searchable fields change
- [x] **T5.4** `catalog_search(tenant_id, query, filters, top_k)` — semantic vector search + metadata filter `REQ-NF4`
- [x] **T5.5** `build_system_prompt(tenant_id)` — compile from persona + schema + guardrails + enabled actions/components `REQ-AI1–AI10, REQ-S10`
- [x] **T5.6** Cache compiled prompt per tenant in Redis; invalidate on config/schema change `REQ-NF-CACHE1`
- [x] **T5.7** Inject Component DSL spec into system prompt `REQ-AI11, REQ-AI12`

---

## Phase 6 — AI Engine & Chat API

### 6A — LLM Provider Adapter
- [x] **T6.1** Define `LLMProvider` abstract interface: `stream_chat(messages, system_prompt) → AsyncIterator[str]` `REQ-AI-P8`
- [x] **T6.2** Implement `GeminiAdapter` (Gemini 2.0 Flash via google-genai SDK) `REQ-AI-P3`
- [x] **T6.3** Implement `OpenAIAdapter` (GPT-4o / GPT-4o-mini) `REQ-AI-P3`
- [x] **T6.4** Implement `PlatformManagedAdapter` (default; delegates to configured provider) `REQ-AI-P1`
- [x] **T6.5** BYOK key: provider factory resolves from tenant config `REQ-AI-P4, REQ-NF16`
- [x] **T6.6** Circuit breaker on all LLM calls (50% error / 30s → open; 60s cooldown) `REQ-NF-CB1–CB3`
- [x] **T6.7** Keyword search fallback when circuit is open `REQ-NF9`

### 6B — Chat SSE Endpoint
- [x] **T6.8** `POST /chat/message` → SSE stream (validate → rate limit → system prompt → vector search → LLM → parse DSL → stream) `REQ-C7, REQ-AI11–AI16, REQ-NF2`
- [x] **T6.9** Multi-step intent resolution: plan → stream sub-step progress → confirm before write steps `REQ-AI-MS1–MS3`
- [x] **T6.10** Log each turn: session_id, tenant_id, input, output_summary, token_count `REQ-AN1, REQ-AN2`
- [x] **T6.11** Async write token usage to append-only usage ledger `REQ-PR5`

### 6C — Session Management
- [x] **T6.12** `POST /sessions` — create anonymous session (UUID, Redis + DB) `REQ-C3, REQ-NF-SL1`
- [x] **T6.13** Session holds: tenant_id, conversation_history (last N turns), active_filters
- [x] **T6.14** `DELETE /sessions/{id}` — clear/reset conversation `REQ-C9`

---

## Phase 7 — Component DSL & Angular Renderer

### 7A — DSL Types
- [x] **T7.1** Define `ComponentSpec` TypeScript union in `libs/shared/constants` (10 component types) `REQ-AI11`
- [x] **T7.2** Write JSON Schema per component type (server-side validation) `REQ-AI15`
- [x] **T7.3** `validateComponentSpec(raw): ComponentSpec | null` util `REQ-AI15`

### 7B — Angular Components (in `libs/frontend/dsl-renderer`)
- [x] **T7.4** `DslRendererComponent` — dispatcher on `type` field `REQ-AI11`
- [x] **T7.5** `ItemCardComponent` — image, title, badges, price, action buttons (3 variants) `REQ-6.1`
- [x] **T7.6** `ItemGridComponent` — responsive 2–6 item grid `REQ-6.2`
- [x] **T7.7** `ItemDetailComponent` — full detail, all displayable fields `REQ-6.3`
- [x] **T7.8** `ComparisonTableComponent` — side-by-side 2–4 items `REQ-6.4`
- [x] **T7.9** `FilterSummaryComponent` — dismissible filter chips `REQ-6.5`
- [x] **T7.10** `ResultCountComponent` — "Showing X of Y items" `REQ-6.6`
- [x] **T7.11** `EmptyStateComponent` — configurable no-results `REQ-6.7`
- [x] **T7.12** `ActionConfirmComponent` — confirmation before write actions `REQ-6.8`
- [x] **T7.13** `InfoBannerComponent` — styled informational block `REQ-6.9`

---

## Phase 8 — Actions Engine

- [ ] **T8.1** `POST /actions/save_item` — persist item + snapshot keyed by session_id `REQ-A7, REQ-A10`
- [ ] **T8.2** `GET /actions/saved_items?session_id=` — retrieve saved items `REQ-A8, REQ-A9`
- [ ] **T8.3** `POST /actions/contact_enquiry` — submit form; route to email/webhook `REQ-A4, REQ-A5`
- [ ] **T8.4** Pre-execution permission check vs tenant config on all actions `REQ-A-DET2`
- [ ] **T8.5** Append-only audit log for every action execution `REQ-A-DET3`
- [ ] **T8.6** Retry transient failures 3× with exponential backoff `REQ-A-DET4`

---

## Phase 9 — AI Config API & UI

- [ ] **T9.1** `GET/PATCH /config/ai` — persona name, tone, custom instruction, welcome message, starter prompts `REQ-AI1–AI5`
- [ ] **T9.2** `GET/PATCH /config/ai/provider` — BYOK toggle, provider, encrypted key `REQ-AI-P1–AI-P8`
- [ ] **T9.3** `GET/PATCH /config/ai/guardrails` — out-of-scope message, recommendation mode, language `REQ-AI6–AI10`
- [ ] **T9.4** `GET/PATCH /config/components` — enable/disable component types `REQ-CM1–CM3`
- [ ] **T9.5** `GET/PATCH /config/actions` — enable/disable actions, labels, enquiry form fields `REQ-A1–A5`
- [ ] **T9.6** Admin chat: AI config panels rendered as inline components `REQ-AN0b`

---

## Phase 10 — Branding & Theming

- [ ] **T10.1** `GET/PATCH /config/branding` — logo, colors, background style, fonts, favicon, page title `REQ-B1–B9`
- [ ] **T10.2** `POST /config/branding/logo` — upload logo to Cloud Storage `REQ-B1`
- [ ] **T10.3** WCAG AA 4.5:1 contrast check on color save — warn + block on fail `REQ-B10`
- [ ] **T10.4** Store theme values as design tokens; inject as CSS custom properties per tenant `REQ-B12`
- [ ] **T10.5** `GET/POST/PATCH /config/themes` — named theme CRUD (max 5); designate default `REQ-B6, REQ-B7`
- [ ] **T10.6** Angular `ThemeService` — load tenant theme on init; apply CSS vars
- [ ] **T10.7** `?theme=` and `?lang=` URL param override handling `REQ-H5`
- [ ] **T10.8** End-user theme/font/bubble-style picker (when admin-enabled) `REQ-C11–C16`
- [ ] **T10.9** Persist end-user preferences to `localStorage` `REQ-C15`

---

## Phase 11 — Admin Dashboard (Chat-Paradigm)

- [ ] **T11.1** Admin shell route `/admin` with Firebase auth guard `REQ-T7`
- [ ] **T11.2** Admin chat interface — same component, admin system prompt + admin-only components `REQ-AN0, REQ-AN0a`
- [ ] **T11.3** Admin assistant renders config components inline (schema form, color picker, metrics) `REQ-AN0b`
- [ ] **T11.4** Traditional form fallbacks for all config sections `REQ-AN0c`
- [ ] **T11.5** Admin sidebar: Dashboard | Catalog | Schema | AI Config | Branding | Actions | Analytics `REQ-T7`
- [ ] **T11.6** Invite admin flow: email → Firebase invite → role assignment `REQ-T8`
- [ ] **T11.7** BYOK warning banner when BYOK is active `REQ-AI-P5`

---

## Phase 12 — Analytics & Usage

- [ ] **T12.1** Daily aggregation jobs: conversations, messages, most-queried items, top intents, action rates, zero-result queries `REQ-AN1–AN6`
- [ ] **T12.2** `GET /analytics/summary?from=&to=` — aggregated metrics for tenant `REQ-AN9`
- [ ] **T12.3** Metrics dashboard component (admin chat) with date range filter `REQ-AN9`
- [ ] **T12.4** Token usage vs plan limit breakdown `REQ-AN7, REQ-PR6`
- [ ] **T12.5** Billing report: seat count, token count, estimated cost `REQ-PR6`
- [ ] **T12.6** Platform admin (L0) rollup: all-tenant usage, revenue vs LLM cost, BYOK split `REQ-AN10–AN12`

---

## Phase 13 — Chat Interface Polish (End User)

- [ ] **T13.1** Starter prompt chips shown when conversation is empty `REQ-AI5, REQ-C6`
- [ ] **T13.2** Typing indicator animation (3-dot pulse) while streaming `REQ-C7`
- [ ] **T13.3** Long-wait message after 10s LLM delay (configurable text) `REQ-C8`
- [ ] **T13.4** Clear/reset conversation button `REQ-C9`
- [ ] **T13.5** Configurable placeholder text on input `REQ-C5`
- [ ] **T13.6** Full keyboard navigation + ARIA labels on all components `REQ-NF17, REQ-NF18`
- [ ] **T13.7** WCAG AA contrast compliance pass on all built-in themes `REQ-NF16`

---

## Phase 14 — Non-Functional & DevOps

- [ ] **T14.1** Cloud Run deployment: backend `Dockerfile` + `cloudbuild.yaml`
- [ ] **T14.2** Firebase Hosting / Cloud Run for Angular SSR shell
- [ ] **T14.3** MongoDB Atlas cluster (M10+); enable Vector Search index
- [ ] **T14.4** Upstash Redis for rate limiting + prompt cache
- [ ] **T14.5** GitHub Actions CI: lint + test on PR; build + deploy on merge to `main`
- [ ] **T14.6** `/health/live` + `/health/ready` endpoints (DB + Redis checks) `REQ-NF8`
- [ ] **T14.7** Structured JSON logging to Cloud Logging; alerts for circuit breaker open `REQ-NF-CB3`
- [ ] **T14.8** TLS via Cloud Run; enforce HTTPS-only `REQ-NF11`
- [ ] **T14.9** Performance baseline: chat load time, LLM TTFT, catalog search latency `REQ-NF1–NF4`
- [ ] **T14.10** E2E Playwright: new conversation, catalog search + component render, save_item action

---

## Summary

| Phase | Area | Tasks | Status |
|---|---|---|---|
| 0 | Foundation / Scaffolding | 12 | ✅ Done |
| 1 | Infrastructure & Auth | 14 | 🔶 13/14 |
| 2 | Tenant Management API | 6 | ✅ Done |
| 3 | Catalog Schema (Import-First) | 7 | ✅ Done |
| 4 | Catalog Data Management | 7 | ✅ 7/7 (MVP) |
| 5 | Vector Search & System Prompt | 7 | ✅ Done |
| 6 | AI Engine & Chat API | 14 | ✅ Done |
| 7 | Component DSL & Renderer | 13 | ✅ Done |
| 8 | Actions Engine | 6 | ⬜ |
| 9 | AI Config API & UI | 6 | ⬜ |
| 10 | Branding & Theming | 9 | ⬜ |
| 11 | Admin Dashboard | 7 | ⬜ |
| 12 | Analytics & Usage | 6 | ⬜ |
| 13 | Chat UI Polish | 7 | ⬜ |
| 14 | DevOps & NFRs | 10 | ⬜ |
| **Total** | | **131** | **80 done** |
