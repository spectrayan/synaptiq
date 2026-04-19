# PRD Gap Analysis
## Original PRD (`PRODUCT REQUIREMENTS DOCUMENT.txt`) vs. MVP Requirements (`requirements.md`)

---

## Summary

| Category | Count |
|---|---|
| In original PRD, **missing** from MVP requirements | 14 items |
| In MVP requirements, **beyond** the original PRD | 18 items |
| **Aligned** in both | 21 items |
| Original PRD items that are **correctly deferred** to Phase 2 | 8 items |

---

## 1. In the Original PRD — Missing from MVP Requirements

These are requirements from your original PRD that are **not yet captured** in the MVP requirements doc. Each needs a decision: incorporate, defer, or drop for MVP.

### 1.1 Data Layer — Much Broader in Original PRD

The original PRD envisions a **general-purpose semantic data layer**, not just a product catalog:

| Original PRD Item | Status in MVP Req | Recommendation |
|---|---|---|
| SQL databases (Postgres, MySQL, Snowflake, BigQuery) | ❌ Not mentioned | **Defer** — MVP uses catalog-specific ingestion (CSV, Sheets, webhook). Full SQL connectors are Phase 2+ |
| NoSQL (MongoDB, DynamoDB) | ❌ Not mentioned | **Defer** — Phase 2+ |
| SaaS APIs (Salesforce, HubSpot, Zendesk, Jira) | ❌ Not mentioned | **Defer** — Phase 2+ |
| Event streams (Kafka, Kinesis) | ❌ Not mentioned | **Defer** — Enterprise tier, not MVP |
| Semantic entity/metric/dimension model | ❌ Not mentioned | **Partially deferred** — The MVP schema builder (Section 2) covers entities but not metrics/dimensions. Add a note that the schema builder is the MVP seed of this concept. |
| Row-level & field-level permissions | ❌ Not mentioned | **Add to MVP** — Important even for catalog (e.g., admin sees cost price, user doesn't). Add as REQ-S-PERM. |
| Data lineage tracking | ❌ Not mentioned | **Defer** — Phase 2+ |
| Business vocabulary mapping ("GMV", "Bookings") | ❌ Not mentioned | **Incorporate as Phase 2** — The custom system instruction (REQ-AI3) partially covers this but it's not structured. |

### 1.2 AI Reasoning Layer — Query Generation Not Addressed

| Original PRD Item | Status in MVP Req | Recommendation |
|---|---|---|
| LLM generates SQL queries | ❌ Not mentioned | **Not applicable for MVP** — MVP uses vector search + structured catalog, no SQL. Relevant when SQL connectors added. |
| LLM generates API calls | ❌ Not mentioned | **Partially covered** — REQ-D3 (webhook ingest) covers inbound. Outbound API calls by LLM are Phase 2. |
| Multi-step task handling | ❌ Not mentioned | **Add to MVP** — E.g., "compare these 3 then save the best one" is a multi-step flow. Add as REQ-AI-MS. |
| Time-series queries | ❌ Not mentioned | **Not applicable** — Product catalog doesn't need this. Relevant in analytics vertical. |

### 1.3 UI Components — Missing from MVP Component Library

The original PRD lists components the MVP requirements don't have:

| Missing Component | In MVP Req? | Recommendation |
|---|---|---|
| Charts (bar, line, pie, heatmap) | ❌ No | **Defer** — Not needed for product catalog. Critical for analytics vertical (Phase 2). |
| KPI cards | ❌ No | **Add** — Useful even for catalog (e.g., "Total items: 247", "Most viewed: iPhone 15"). Add as `kpi-card` component. |
| Multi-step workflow component | ❌ No | **Defer** — Phase 2 workflow engine. |
| Modals / Tabs | ❌ No | **Partially covered** — `item-detail` serves the modal use case. Tabs deferred. |
| Forms (AI-generated) | ❌ Partially | MVP has `contact_enquiry` form only. General-purpose form generation is Phase 2. |
| Dashboard layout (multi-component canvas) | ❌ No | **Defer** — MVP renders components inline in chat. Pinned dashboard canvas is Phase 2. |

### 1.4 Workflow Engine — Entirely Missing from MVP

| Original PRD Item | Status | Recommendation |
|---|---|---|
| CRUD operations via chat | ❌ Not in MVP | **Partially add** — `save_item` is a write operation. General CRUD against the catalog (edit item, add item via chat) could be a valuable MVP feature for the admin interface. Add to admin chat. |
| AI-generated workflow rules | ❌ Not in MVP | **Defer** — Complex. Phase 2. |
| Notifications / Alerts | ❌ Not in MVP | **Defer** — Phase 2. |
| Approvals | ❌ Not in MVP | **Defer** — Phase 2. |
| Scheduled jobs | ❌ Not in MVP | **Defer** — Phase 2. |
| Deterministic execution layer | ❌ Not explicitly stated | **Add** — Important safety requirement. The action execution layer must be deterministic (same action = same result), permission-aware, logged, with retry logic. Incorporate into Section 7. |

### 1.5 Performance Targets — Original PRD is Stricter

| Metric | Original PRD | MVP Requirements | Gap |
|---|---|---|---|
| UI render | < 200ms | < 100ms (component render) | ✅ MVP is stricter |
| Query execution | < 2s | < 500ms (catalog search) | ✅ MVP is stricter |
| Chat response | < 1.5s (streaming) | < 3s first token | ⚠️ **Gap** — Original PRD targets 1.5s. MVP says 3s. Tighten to 2s for MVP. |
| Workflow execution | < 500ms | N/A | N/A — no workflows in MVP |

### 1.6 Security — Original PRD Has Higher Bar

| Original PRD Item | MVP Req | Gap |
|---|---|---|
| SOC2 compliance | ❌ Not mentioned | **Add as a roadmap item** — Not achievable at MVP launch, but must be planned for. Add to future enhancements. |
| SSO (SAML, OAuth) | Partial — OAuth only | **Add SAML** to Phase 2 scope. Enterprise clients will require it. |
| Zero-trust architecture | ❌ Not mentioned | **Add as NFR** — All internal service calls authenticated, no implicit trust. |
| Field-level encryption | ❌ Not mentioned | **Add** — Especially important for BYOK key storage (already partially covered) and sensitive catalog fields. |
| Circuit breakers | ❌ Not mentioned | **Add to REQ-NF** — Important for LLM API resilience. |
| Rate limiting | ❌ Not mentioned | **Add** — Per-tenant and per-user rate limiting to prevent abuse. |
| Auto-healing / Disaster recovery | ❌ Not mentioned | **Add** — At least basic: health checks, auto-restart, DB backups. |

### 1.7 Scalability — Original PRD is More Ambitious

| Original PRD Item | MVP Req | Gap |
|---|---|---|
| Horizontal scaling | ❌ Not mentioned | **Add as architectural constraint** — Services must be stateless to allow horizontal scale. |
| Stateless services | ❌ Not mentioned | **Add as architectural NFR** |
| Distributed caching | ❌ Not mentioned | **Add** — Required for performance under load (cache LLM system prompts, catalog embeddings). |
| Event-driven architecture | ❌ Not mentioned | **Add as architectural direction** — Usage ledger (REQ-PR5) already implies this. Make explicit. |

### 1.8 Reliability — Original PRD Targets 99.9% vs MVP's 99.5%

> [!WARNING]
> The original PRD targets **99.9% uptime** (~8.7 hours downtime/year).
> The MVP requirements say **99.5%** (~43 hours downtime/year).
> This gap should be intentional — confirm 99.5% is acceptable for MVP launch or align to 99.9%.

---

## 2. In MVP Requirements — Beyond the Original PRD

These are items in the new MVP requirements that **add meaningful detail or new concepts** not in the original PRD. These are all good additions.

| MVP Addition | Value |
|---|---|
| Three-tier stakeholder model (L0/L1/L2) | Clearer than the original PRD's implicit model |
| Dynamic catalog schema builder (Section 2) | The original PRD mentions entities/metrics abstractly — MVP spec makes this concrete and implementation-ready |
| BYOK API key model with marketing strategy | Original PRD only says "platform-managed" implicitly |
| Component DSL / Output Contract (REQ-AI11–16) | Original PRD says "LLM outputs strict schema" but doesn't define the contract — MVP does |
| Seat + Token pricing instrumentation | Original PRD lists pricing options but no implementation spec |
| Admin dashboard dogfooding chat paradigm | Not in original PRD — strong differentiator |
| `item_snapshot` on save_item | Handles catalog mutations over time — not in original PRD |
| Vector search specification | Original PRD doesn't specify search type |
| Tenant subdomain hosting spec | Not in original PRD |
| WCAG 2.1 AA accessibility requirement | Not in original PRD |
| Schema field attributes (searchable, filterable, sortable, displayable) | Much more detailed than original PRD |
| End-user personalization controls scoped by business admin | Not in original PRD |
| `contact_enquiry` action with webhook/email routing | Not in original PRD |
| Zero-result query analytics | Not in original PRD |
| BYOK vs platform-managed billing split | Not in original PRD |
| Tenant status lifecycle (active/suspended/trial/onboarding) | Not in original PRD |
| Atomic config save requirement | Not in original PRD |
| LLM fallback to keyword search on outage | Not in original PRD |

---

## 3. Aligned — In Both Documents

| Topic | Both Agree |
|---|---|
| Chat is the primary UI surface, not a feature | ✅ |
| LLM outputs structured schemas, not raw HTML/code | ✅ |
| Dynamic UI rendering from schema | ✅ |
| Multi-tenant architecture | ✅ |
| Role-based access control | ✅ |
| Audit logging | ✅ |
| Suggested prompts / autocomplete | ✅ |
| Context retention / multi-turn reasoning | ✅ |
| Streaming responses | ✅ |
| Mobile app deferred | ✅ |
| Voice interface deferred | ✅ |
| Personalization (user preferences, saved views) | ✅ |
| Admin console separate from end-user interface | ✅ |
| Action execution must be permission-aware and logged | ✅ |
| Schema validation & guardrails (no hallucinated fields) | ✅ |
| Future: plugin marketplace, embeddable widgets | ✅ |
| Future: predictive analytics, AI-generated onboarding | ✅ |
| Future: multi-agent collaboration | ✅ |

---

## 4. Recommended Additions to MVP Requirements

Based on the gap analysis, these items from the original PRD should be added to the MVP requirements doc now:

> [!IMPORTANT]
> **High Priority Additions** — Should be in MVP:

1. **REQ-S-PERM**: Field-level visibility permissions — certain schema fields can be marked `admin_only`, hiding them from end-user rendered components even if they exist in the catalog data
2. **REQ-A-DET**: Action execution layer is deterministic, permission-checked, and fully logged with retry logic on transient failures
3. **REQ-AI-MS**: The LLM supports multi-step intent resolution within a single conversation turn (e.g., "compare these 3, then save the cheapest one")
4. **REQ-NF-RL**: Per-tenant and per-session rate limiting on LLM calls to prevent abuse and runaway token costs
5. **REQ-NF-CB**: Circuit breaker on the LLM provider integration — if the provider returns errors > threshold, requests fail fast with graceful degradation
6. **REQ-NF-CACHE**: LLM system prompt and catalog embedding vectors are cached per tenant; cache invalidated on schema or catalog update
7. **REQ-NF-ZT**: All internal service-to-service calls are authenticated (zero-trust); no service is implicitly trusted by another
8. **REQ-NF-SL**: Services are stateless — no session state held in memory; all state in DB/cache to allow horizontal scaling

> [!NOTE]
> **Roadmap Additions** — Note in future enhancements:

9. SOC2 Type II compliance (target: 12 months post-launch)
10. SAML SSO for enterprise tenants
11. Full SQL/NoSQL data source connectors
12. AI-generated workflow rules engine
13. Dashboard canvas (multi-component pinned layout)
14. Chart components (bar, line, pie) — critical for analytics vertical

---

## 5. Scope Difference Summary

The original PRD is a **platform-level vision document** for the full product. The MVP requirements are scoped to the **product catalog vertical** as a proving ground.

This is the right strategy. The key is ensuring the MVP architecture doesn't create dead ends:

```
Original PRD Vision
  └── Analytics dashboards          → MVP seeds this with: kpi-card component, zero-result analytics
  └── Workflow engine                → MVP seeds this with: action execution layer, deterministic actions
  └── Semantic data model            → MVP seeds this with: dynamic schema builder
  └── SQL/API connectors             → MVP seeds this with: webhook ingest, REST API data source
  └── Multi-agent collaboration      → MVP seeds this with: LLM provider abstraction layer
```

The MVP is not a throwaway — it's the foundation. Every architectural decision in the MVP should be made with the full PRD vision in mind.
