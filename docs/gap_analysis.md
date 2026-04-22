# Synaptiq MVP — Gap Analysis

> **Generated**: 2026-04-20  
> **Compared against**: [requirements.md](file:///d:/git/synaptiq/docs/requirements.md), [architecture.md](file:///d:/git/synaptiq/docs/architecture.md), [tasks.md](file:///d:/git/synaptiq/docs/tasks.md)

---

## Executive Summary

The Synaptiq MVP is **substantially implemented**. The task tracker claims 116/131 tasks done. After auditing the actual codebase against the requirements document, the implementation is strong across the core value prop (chat→AI→DSL→components pipeline), but gaps exist primarily in **DevOps/deployment**, **analytics aggregation jobs**, **security hardening**, and a few **missing feature details**. The architecture doc also has some inconsistencies with the actual implementation that should be reconciled.

### Score by Area

| Area | Requirement Coverage | Code Quality | Notes |
|---|---|---|---|
| Tenant Management | ✅ 95% | Solid | Admin invite flow incomplete |
| Catalog Schema | ✅ 100% | Strong | Import-first approach well-built |
| Catalog Data | ✅ 90% | Good | Webhook + Sheets deferred by design |
| AI Engine | ✅ 90% | Strong | No Anthropic adapter; no multi-step execution |
| Chat Interface | ✅ 90% | Excellent | Long-wait message missing |
| Component DSL | ✅ 100% | Excellent | 13 components incl. extras |
| Actions Engine | ✅ 85% | Good | Webhook dispatch is TODO; retries not wired |
| Branding/Theming | ✅ 95% | Excellent | Full theme lifecycle |
| Analytics | ⚠️ 70% | Partial | No aggregation jobs; key metrics stubbed |
| DevOps/NFRs | ❌ 20% | Minimal | Major gap — nothing deployed |
| Security | ⚠️ 60% | Partial | BYOK encryption, zero-trust, JWTs missing |

---

## Section 1 — Fully Implemented ✅

These requirement areas are well-implemented with working code:

### 1.1 Tenant Management (REQ-T1 through REQ-T8)
- ✅ Tenant CRUD with unique `tenant_id`, name, slug/subdomain
- ✅ Tenant status lifecycle (`active`, `suspended`, `trial`, `onboarding`)
- ✅ Default config auto-provisioning — seed scripts handle this
- ✅ Per-tenant limits (max items, tokens, users, requests)
- ✅ Role-based admin access (Owner, Editor, Viewer)
- ✅ Firebase Auth integration (email/password + Google OAuth)
- ✅ Auth middleware for protected routes

### 1.2 Catalog Schema (REQ-S1 through REQ-S12)
- ✅ Custom schema builder with all field attributes (`field_id`, `label`, `type`, `required`, `searchable`, `filterable`, `sortable`, `displayable`, `visibility`)
- ✅ Schema import from OpenAPI/JSON Schema/YAML
- ✅ Primary label/image/price designators
- ✅ Schema versioning and `admin_only` field stripping
- ✅ Prompt cache invalidation on schema change

### 1.3 Catalog Data Management (REQ-D1 through REQ-D10)
- ✅ Item CRUD with schema validation
- ✅ CSV import with per-row validation
- ✅ Bulk operations (activate/deactivate/archive)
- ✅ Max item limit enforcement per tenant

### 1.4 AI Engine (REQ-AI1 through REQ-AI16)
- ✅ Configurable persona (name, tone, custom instruction, welcome message, starter prompts)
- ✅ LLM provider abstraction layer
- ✅ Gemini adapter (google-genai SDK) — `GeminiAdapter`
- ✅ OpenAI adapter (GPT-4o/mini) — `OpenAIAdapter`
- ✅ Platform-managed adapter with dev fallback — `PlatformManagedAdapter`, `DevEchoAdapter`
- ✅ Circuit breaker (50% error / 30s → open; 60s cooldown) — `CircuitBreaker`
- ✅ Keyword search fallback when circuit is open
- ✅ Component DSL output contract (Component Spec JSON)
- ✅ Guardrails config (out-of-scope message, recommendation mode, language)
- ✅ System prompt compilation from config

### 1.5 Chat Interface (REQ-C1 through REQ-C10)
- ✅ Chat-only application (no other screens) — routes redirect everything to `/chat`
- ✅ SSE streaming with real-time token display
- ✅ Conversation session management (create/list/delete/history)
- ✅ Typing indicator (3-dot pulse)
- ✅ Starter prompt chips with DSL suggestion chips
- ✅ Clear/reset conversation
- ✅ Configurable placeholder text

### 1.6 Component DSL & Renderer (REQ-6.1 through REQ-CM3)
- ✅ All 10 specified components implemented plus 3 extras:
  1. `ItemCardComponent` (3 variants: standard, compact, featured)
  2. `ItemGridComponent` (responsive 2–6 columns)
  3. `ItemDetailComponent`
  4. `ComparisonTableComponent`
  5. `FilterSummaryComponent`
  6. `ResultCountComponent`
  7. `EmptyStateComponent`
  8. `ActionConfirmComponent`
  9. `InfoBannerComponent`
  10. `DataTableComponent` (bonus)
  11. `SuggestionBarComponent` (bonus)
  12. `FormInputComponent` (bonus — 9 field types)
  13. `DslRendererComponent` (dispatcher)
- ✅ `validateComponentSpec()` utility
- ✅ Component enablement config per tenant

### 1.7 Branding & Theming (REQ-B1 through REQ-B12)
- ✅ Logo upload (PNG/SVG)
- ✅ Primary/secondary color, background style, fonts
- ✅ Named theme presets (max 5, one default)
- ✅ WCAG AA 4.5:1 contrast checking with warn
- ✅ Design tokens as CSS custom properties
- ✅ Dynamic Google Fonts loading
- ✅ `?theme=` and `?lang=` URL param overrides
- ✅ End-user preferences persisted to `localStorage`
- ✅ Favicon and page title management

### 1.8 Session & Rate Limiting (REQ-NF-RL1 through REQ-NF-RL4)
- ✅ Redis-backed sliding window rate limiter
- ✅ Per-tenant (60 req/min) and per-session (10 req/min) limits
- ✅ Graceful in-chat error messages (not HTTP 429)

### 1.9 Vector Search & Embeddings (T5.1 through T5.7)
- ✅ Gemini `text-embedding-004`
- ✅ OpenAI fallback for BYOK
- ✅ Embed on item create/update, re-embed on schema change
- ✅ Catalog vector search with min_score filtering
- ✅ System prompt caching in Redis with TTL

---

## Section 2 — Partial Implementation ⚠️

### 2.1 Anthropic Claude Adapter — Missing
**Requirement**: REQ-AI-P3, architecture.md Section 9

| What's documented | What exists |
|---|---|
| `AnthropicAdapter(LLMAdapter)` class in architecture.md | ❌ Not implemented |
| Anthropic as a BYOK option in `get_adapter()` | ❌ `get_provider()` has no Anthropic branch |
| `LLMProviderType.anthropic` enum value | ✅ Exists in tenant model but no adapter code |

**Effort**: Low (~2 hours). Similar structure to `OpenAIAdapter`, using the `anthropic` Python SDK.

---

### 2.2 Multi-Step Intent Resolution — Missing
**Requirements**: REQ-AI-MS1, REQ-AI-MS2, REQ-AI-MS3

| What's documented | What exists |
|---|---|
| Multi-step execution with visible sub-steps | ❌ No multi-step planner |
| User can cancel before write steps | ❌ Not implemented |
| Each step individually logged in audit trail | ❌ Not implemented |

> **Note:** Task T6.9 is marked `[x]` (complete) in tasks.md, but no multi-step intent resolution code exists anywhere in the backend. This is either deferred or the task was mislabeled.

**Effort**: High (~2-3 weeks). Requires an intent planning layer, step execution engine, and frontend progress UI.

---

### 2.3 Analytics Aggregation Jobs — Not Implemented
**Requirement**: T12.1 (REQ-AN1 through REQ-AN6)

| Metric | Status |
|---|---|
| Total conversations (daily/weekly/monthly) | ⚠️ Computed on-the-fly, no pre-aggregation |
| Total messages by end users | ✅ Via usage_ledger |
| Most queried items | ❌ Returns `[]` — `# TODO: implement search log tracking` |
| Most common search intents (top 10) | ❌ Returns `[]` |
| Action conversion rates | ✅ Aggregated from action_logs |
| Zero-result queries | ❌ Returns `[]` |

**Effort**: Medium (~1 week). Needs: (1) search log collection in chat_service, (2) daily aggregation job, (3) materialized analytics collection.

---

### 2.4 BYOK API Key Encryption — Not Implemented
**Requirements**: REQ-AI-P4, REQ-NF16

BYOK keys would currently be stored in plaintext in MongoDB. This is a **security gap** that must be fixed before launch.

**Effort**: Medium (~3-4 days).

---

### 2.5 Zero-Trust / Inter-Service Auth — Not Implemented  
**Requirements**: REQ-NF-ZT1, REQ-NF-ZT2. N/A for MVP (monolith).

### 2.6 Contact Enquiry Webhook Dispatch — TODO  
**Requirement**: REQ-A5. `# TODO: fire tenant webhook if configured`. **Effort**: Low (~4 hours).

### 2.7 Action Retry with Exponential Backoff — Not Verified  
**Requirement**: REQ-A-DET4 (T8.6). **Effort**: Low (~4 hours).

### 2.8 Admin Invite Flow via Firebase — Incomplete  
**Requirements**: REQ-T8 (T11.6). **Effort**: Medium (~2-3 days).

### 2.9 BYOK Warning Banner — Not Implemented  
**Requirement**: REQ-AI-P5 (T11.7). **Effort**: Low (~2 hours).

### 2.10 Long-Wait Message (>10s LLM Delay) — Not Implemented  
**Requirement**: REQ-C8 (T13.3). **Effort**: Low (~3 hours).

### 2.11 Health Check — Partial  
**Requirement**: REQ-NF8 (T14.6). Readiness probe is a stub. **Effort**: Low (~1 hour).

---

## Section 3 — Not Implemented ❌

### 3.1 Entire DevOps & Deployment Pipeline (Phase 14)

All 10 tasks in Phase 14 are marked `[ ]` (not done). No CI/CD, no production deployment, no monitoring, no E2E tests.

### 3.2 WCAG AA Compliance Pass — Not Done  
**Requirement**: REQ-NF16 (T13.7).

### 3.3 Backend Tests — Empty  
Zero backend tests. The `tests/` directory contains only an empty `__init__.py`.

---

## Section 4 — Architecture Doc Inconsistencies

| Doc Says | Reality | Impact |
|---|---|---|
| Frontend: **Next.js** (Sections 2, 11) | Frontend: **Angular 21** with Nx | 🟡 Stale references |
| DB: **PostgreSQL + pgvector** (Section 2 diagram) | DB: **MongoDB Atlas** | 🟡 Stale references |
| BYOK supports: **OpenAI, Gemini, Anthropic** | Only **OpenAI + Gemini** adapters exist | 🔴 Missing |
| Redis: **Upstash Redis** (HTTP-based) | Standard Redis (`redis-py` with TCP) | 🟡 Dev vs prod difference |

---

## Section 5 — Deferred by Design

Webhook import, Google Sheets sync, custom domain CNAME, website embed script, voice input, end-user authentication, multi-language auto-detection — all explicitly documented as Phase 2.

---

## Section 6 — Priority Matrix

### 🔴 Critical (Before deployment)
1. BYOK key encryption (3-4 days)
2. CI/CD pipeline (2-3 days)
3. Production deployment (3-5 days)
4. Backend test coverage (5-7 days)
5. Health check readiness probe (1 hour)

### 🟡 High (Before MVP launch)
6. Analytics aggregation jobs + missing metrics (1 week)
7. Structured logging + circuit breaker alerts (2-3 days)
8. Admin invite flow (2-3 days)
9. Contact enquiry webhook dispatch (4 hours)
10. Anthropic adapter (2 hours)
11. Action retry with backoff (4 hours)
12. Architecture doc cleanup (2 hours)

### 🟢 Low (Before GA)
13. Multi-step intent resolution (2-3 weeks)
14. Long-wait message (3 hours)
15. BYOK warning banner (2 hours)
16. WCAG AA compliance audit (3-5 days)
17. E2E Playwright tests (3-5 days)
18. Performance baselines (2-3 days)
