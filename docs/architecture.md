# Architecture & Tech Stack
## Spectrayan Platform — AI-Native Data Application Platform

> **Resolved Decisions:** Angular 21 + Angular Material 3 frontend (no Tailwind — M3 theming + custom utility layer), MongoDB Atlas, Nx monorepo, `*.spectrayan.com` subdomains, per-tenant embedding providers, Redis + MongoDB for conversation history, single Firebase project for all auth.

---

## 1. Guiding Architectural Principles

| Principle | Implementation |
|---|---|
| Nothing hardcoded | Schema-driven everywhere — components, fields, actions all from config |
| Chat is the OS | No page routing — everything renders inside the chat surface |
| LLM outputs intent, not data | LLM outputs Component DSL specs + item IDs; backend hydrates with real data |
| Stateless services | All state in DB/cache; services scale horizontally without coordination |
| Zero-trust | Every inter-service call authenticated; no implicit trust |
| Tenant isolation | Row-level tenant scoping on every DB query; separate vector namespaces |

---

## 2. System Architecture Overview

```
                        ┌─────────────────────────────┐
                        │   Cloudflare (DNS + CDN)     │
                        │   Wildcard: *.platform.com   │
                        └──────────────┬──────────────┘
                                       │
               ┌───────────────────────┼───────────────────────┐
               │                       │                       │
    ┌──────────▼────────┐  ┌──────────▼────────┐  ┌──────────▼────────┐
    │  acme.platform.com│  │  beta.platform.com│  │ admin.platform.com│
    │  (End User Chat)  │  │  (End User Chat)  │  │ (Platform Admin)  │
    └──────────┬────────┘  └──────────┬────────┘  └──────────┬────────┘
               │                       │                       │
               └───────────────────────┼───────────────────────┘
                                       │
                         ┌──────────────▼──────────────┐
                         │    Angular 21 SSR Frontend   │
                         │  (Cloud Run — containerized) │
                         │  Middleware: tenant resolver  │
                         └──────────────┬──────────────┘
                                       │ REST + SSE
                        ┌──────────────▼──────────────┐
                        │   Spring Boot 4 Backend      │
                        │  (Cloud Run — containerized) │
                        │  Modules: chat, datasource,  │
                        │  tenant, actions, usage,     │
                        │  integration, analytics      │
                        └──┬───────────┬──────────────┘
                           │           │
               ┌────────────▼─┐   ┌─────▼────────────┐
               │ MongoDB Atlas │   │  Redis 7            │
               │ + Vector      │   │  (Cache + Rate Limit│
               │   Search      │   │   + Sessions)       │
               └────────────┬─┘   └─────────────────────┘
                           │
              ┌────────────▼──────────────┐
              │  Vertex AI (Gemini)        │
              │  + text-embedding-004      │
              │  OR BYOK (OpenAI/Anthropic)│
              └───────────────────────────┘
```

---

## 3. Technology Stack

### 3.1 Frontend
| Choice | Technology | Rationale |
|---|---|---|
| Framework | **Angular 21** (standalone components, signals, zoneless) | Latest stable, deep team expertise, Nx first-class support, Angular Universal for SSR |
| UI Component Library | **Angular Material 3 (MDC)** | Official M3 components — dialogs, chips, cards, inputs — accessible and brand-flexible out of the box |
| Monorepo | **Nx** | Shared libraries, task orchestration, code generation |
| Styling | **Angular Material 3 theming + `libs/theme` utility layer** | M3 dynamic color tokens for all component styling; ~30-line layout utility layer (`stack`, `cluster`, `grid-*`, `surface`) replaces Tailwind for layout only. No Tailwind — eliminates conflicts with M3's token system |
| State | **Angular Signals + RxJS** | Signals for reactive UI state; RxJS `EventSource` observable for SSE streaming |
| Responsiveness | **Angular CDK BreakpointObserver + CSS Grid** | Programmatic breakpoints for layout switching; M3 components are inherently responsive |
| Auth (Admin) | **Firebase Auth SDK** | Consistent with Firebase ecosystem |
| Component DSL Renderer | Custom `ComponentRendererComponent` | `@switch` on `spec.component` → Angular + Material component |

### 3.2 Backend
| Choice | Technology | Rationale |
|---|---|---|
| Framework | **Spring Boot 4 (WebFlux)** | Reactive non-blocking I/O, strong enterprise ecosystem, Spring AI for LLM integration |
| Architecture | **Spring Modulith** (hexagonal) | Bounded context enforcement, clean port/adapter boundaries, OpenAPI-first contracts |
| LLM Integration | **Spring AI** (Vertex AI Gemini) | Spring-native, provider-agnostic, streaming support |
| API Contracts | **OpenAPI 3.0** (code-first → generated) | SpringDoc generates spec; Angular SDK auto-generated from spec |
| Vector Search | **MongoDB Atlas Vector Search** | Managed service, no separate infra, tenant-scoped |
| Embeddings | **Vertex AI text-embedding-004** | Consistent with GCP; high quality |
| Cache / Rate Limit | **Redis 7** (reactive via Lettuce) | Reactive driver, works with WebFlux |
| Auth validation | **Firebase Admin SDK + JWT** | Verify Firebase tokens on protected routes, built-in JWT fallback |
| Task Queue | **Cloud Tasks** (GCP) | Async jobs: CSV ingestion, embedding generation, analytics rollup |

### 3.3 Data
| Layer | Technology | Purpose |
|---|---|---|
| Primary DB | **MongoDB Atlas** | All data: tenants, schema, data source records, action logs, usage ledger — document model fits dynamic data fields perfectly |
| Vector Index | **MongoDB Atlas Vector Search** | Per-collection vector index on `embedding` field; tenant-scoped via `tenantId` filter |
| Conversation History | **Redis** (TTL 2hr) + **MongoDB** (last 50 turns persisted) | Redis for active sessions; MongoDB fallback for longer context |
| Cache | **Redis 7** | System prompt cache, rate limit counters, tenant config cache |
| Object Storage | **Google Cloud Storage** | Logo uploads, CSV imports, exports |

**Why MongoDB over PostgreSQL:**
- Data source records have dynamic fields defined by the schema builder — documents are the natural model; no `data_json` workarounds needed
- Atlas Vector Search is production-grade managed vector search — no pgvector extension to maintain
- Schema changes (adding data fields) require zero DB migrations
- Multi-document ACID transactions available for billing ledger operations
- Per-tenant embedding dimensions supported: store `embeddingModel` + `embeddingDim` on each document

### 3.4 Infrastructure
| Layer | Technology | Rationale |
|---|---|---|
| Compute | **GCP Cloud Run** | Serverless containers, scales to zero, auto-scales, pay-per-use |
| DNS + CDN | **Cloudflare** | Wildcard `*.platform.com`, DDoS, edge caching |
| LLM (platform) | **Vertex AI — Gemini 1.5 Pro** | GCP-native, strong reasoning, long context window |
| LLM (BYOK) | **OpenAI GPT-4o / Anthropic Claude** | Via unified adapter interface |
| CI/CD | **GitHub Actions** | Build → test → deploy to Cloud Run |
| Monitoring | **GCP Cloud Monitoring + Logging** | Metrics, alerts, log aggregation |
| Secrets | **GCP Secret Manager** | BYOK API keys, DB credentials |

---

## 4. Multi-Tenant Subdomain Routing

```
Request: GET https://acme.platform.com/

Angular Middleware (route guard / interceptor):
  1. Extract tenant slug from Host header → "acme"
  2. Check Redis: GET tenant:acme:config
     ├── HIT  → use cached config (TTL: 5 min)
     └── MISS → fetch from DB → cache → continue
  3. Inject tenant context into request headers
  4. Render chat interface with tenant branding/config

Admin route: https://acme.platform.com/admin/*
  → Requires Firebase Auth token
  → Same tenant context resolution
  → Renders admin chat interface (different component set)

Platform admin: https://admin.platform.com
  → Separate Angular route group
  → Requires platform-level Firebase Auth role
```

**DNS Setup:**
- `*.platform.com` → Cloudflare → Cloud Run (single service)
- Angular SSR middleware handles all tenant resolution

---

## 5. LLM Orchestration Pipeline

This is the critical path for every end-user chat message.

```
User sends: "Show me wireless headphones under $150"

Step 1 — Rate Limit Check (Redis)
  └── Check: session_id rate limit (10 req/min)
  └── Check: tenant rate limit (60 req/min)
  └── FAIL → return graceful in-chat message

Step 2 — Context Build
  ├── Load tenant config (cache-first)
  ├── Load system prompt (cache-first, key: tenant:{id}:prompt:{schema_v})
  └── Load conversation history (last N turns from Redis session store)

Step 3 — Vector Search (data retrieval)
  ├── Embed user message → Vertex AI text-embedding-004
  ├── Query Atlas Vector Search: top-8 similar records WHERE tenant_id = X
  │     filter: status = 'active'
  │     strip: admin_only fields from results
  └── Retrieved items injected into LLM context

Step 4 — LLM Call (streaming)
  ├── Build final prompt: system_prompt + history + retrieved_items + user_message
  ├── Call LLM via adapter (Gemini / BYOK)
  ├── Stream tokens → SSE to frontend
  └── Accumulate full response for parsing

Step 5 — Component DSL Parsing & Validation
  ├── Extract Component Spec blocks from LLM output
  ├── Validate each spec against OpenAPI-generated DTOs (Jackson)
  ├── Hydrate: resolve item_ids → full item data from DB
  │     (strip admin_only fields)
  └── INVALID spec → fall back to plain text, log warning

Step 6 — Token Accounting
  ├── Count input + output tokens
  ├── Append to usage ledger (append-only, async via Cloud Tasks)
  └── Check if tenant token cap exceeded → flag for next request

Step 7 — Response Complete
  └── Emit SSE event: done
  └── Save conversation turn to session store
```

---

## 6. Component DSL Contract

The contract between the LLM and the frontend renderer. The LLM **never** outputs raw HTML or data — only specs.

### 6.1 Envelope Format
Every LLM response is a sequence of `MessagePart` objects:

```json
{
  "parts": [
    { "type": "text", "content": "Here are some great options for you:" },
    {
      "type": "component",
      "spec": {
        "component": "item-grid",
        "props": {
          "layout": "3-col",
          "items": ["item_abc123", "item_def456", "item_ghi789"],
          "highlight": ["price", "rating"]
        }
      }
    },
    { "type": "text", "content": "The Sony WH-1000XM5 has the best battery life." }
  ]
}
```

### 6.2 Component Spec Schema (Java Records)

```java
public record ItemGridSpec(
    @JsonProperty("component") String component,  // "item-grid"
    @JsonProperty("props") ItemGridProps props
) {}

public record ItemGridProps(
    @JsonProperty("layout") String layout,         // "2-col", "3-col", "4-col"
    @JsonProperty("items") List<String> items,      // item_ids only — resolved by backend
    @JsonProperty("highlight") List<String> highlight // field_ids to visually emphasize
) {}

public record ComparisonTableSpec(
    @JsonProperty("component") String component,  // "comparison-table"
    @JsonProperty("props") ComparisonTableProps props
) {}

public record ComparisonTableProps(
    @JsonProperty("items") List<String> items,    // 2–4 item_ids
    @JsonProperty("fields") List<String> fields   // field_ids to compare
) {}

// ... one record class per component type
```

### 6.3 Hydration (Backend)
After parsing the spec, the backend resolves all `item_id` references:

```java
@Service
public class SpecHydrationService {
    private final DataSourceRecordRepository recordRepo;
    private final SchemaFieldService schemaService;

    public Mono<ComponentSpec> hydrate(ComponentSpec spec, String tenantId) {
        List<String> itemIds = extractItemIds(spec);
        return recordRepo.findByIdInAndTenantId(itemIds, tenantId)
            .map(items -> stripAdminOnlyFields(items, schemaService.getSchema(tenantId)))
            .map(items -> injectItemsIntoSpec(spec, items));
    }
}
```

The frontend **never** fetches items — it receives fully hydrated specs.

### 6.4 Streaming Protocol (SSE Events)

```
event: token       data: {"text": "Here are "}
event: token       data: {"text": "some great "}
event: token       data: {"text": "options:"}
event: component   data: {"spec": {...hydrated component spec...}}
event: token       data: {"text": "Sony has the best battery."}
event: done        data: {"usage": {"input_tokens": 1240, "output_tokens": 87}}
event: error       data: {"code": "RATE_LIMITED", "message": "..."}
```

Frontend handles each event type independently, rendering components inline as they arrive.

---

## 7. Database Schema (Core Tables)

```javascript
// tenants collection
{
  _id: ObjectId,
  slug: "acme",                  // unique index
  name: "Acme Corp",
  status: "active",              // active | suspended | trial | onboarding
  planTier: "growth",
  limits: { maxItems: 10000, monthlyTokens: 5000000, maxSeats: 50 },
  branding: { primaryColor: "#6B4FFF", logoUrl: "", headingFont: "Inter" },
  aiConfig: {
    provider: "platform",        // platform | byok
    byokProvider: null,          // openai | anthropic | gemini
    byokKeyEncrypted: null,      // envelope-encrypted
    byokModel: null,
    embeddingProvider: "vertex", // vertex | openai
    embeddingModel: "text-embedding-004",
    embeddingDim: 768,
    assistantName: "Aria",
    tone: "professional",
    language: "en",
    oosMessage: "I can only help with product questions.",
    customInstruction: null
  },
  actionsConfig: { enabledActions: ["save_item", "contact_enquiry"] },
  schemaVersion: 3,
  createdAt: ISODate, updatedAt: ISODate
}

// schemaFields collection
{
  _id: ObjectId,
  tenantId: ObjectId,
  fieldId: "price",              // unique per tenant
  label: "Price",
  type: "currency",              // text | number | currency | boolean | enum | url | image
  visibility: "public",          // public | admin_only | hidden
  isRequired: true,
  isSearchable: true,
  isFilterable: true,
  isSortable: true,
  displayOrder: 3,
  enumValues: null,
  deprecated: false,
  createdAt: ISODate
}

// records collection  (dynamic fields from schema builder live in `fields`)
{
  _id: ObjectId,
  tenantId: ObjectId,
  dataSourceId: ObjectId,
  status: "active",
  fields: {                      // fully dynamic — matches tenant schema
    name: "Example Record",
    description: "Sample data entry",
    category: "General",
    rating: 4.8
  },
  embedding: [0.021, -0.043, ...],  // Atlas Vector Search index
  embeddingModel: "text-embedding-004",
  embeddingDim: 768,
  createdAt: ISODate, updatedAt: ISODate
}
// Atlas Search Index on embedding field (created once per tenant collection or filtered by tenantId)

// savedItems collection
{
  _id: ObjectId,
  tenantId: ObjectId,
  sessionId: "sess_abc",
  itemId: ObjectId,
  itemSnapshot: { /* item fields at time of save */ },
  savedAt: ISODate
}

// actionLogs collection  (append-only)
{
  _id: ObjectId,
  tenantId: ObjectId,
  sessionId: "sess_abc",
  actionId: "contact_enquiry",
  inputSnapshot: { name: "Jane", email: "jane@co.com", message: "..." },
  outcome: "success",            // success | failed | retrying
  error: null,
  retries: 0,
  createdAt: ISODate
}

// usageEvents collection  (append-only)
{
  _id: ObjectId,
  tenantId: ObjectId,
  sessionId: "sess_abc",
  eventType: "token_usage",      // seat_start | token_usage
  seatMonth: "2026-04",
  inputTokens: 1240,
  outputTokens: 87,
  llmProvider: "platform",
  createdAt: ISODate
}

// adminUsers collection
{
  _id: ObjectId,
  tenantId: ObjectId,
  firebaseUid: "uid_abc",
  role: "owner",                 // owner | editor | viewer
  invitedAt: ISODate, createdAt: ISODate
}

// conversationHistory collection  (persisted turns — Redis is primary)
{
  _id: ObjectId,
  tenantId: ObjectId,
  sessionId: "sess_abc",
  turns: [
    { role: "user", content: "Show headphones", ts: ISODate },
    { role: "assistant", content: "...", components: [...], ts: ISODate }
  ],
  lastActiveAt: ISODate,         // TTL index: auto-delete after 30 days
  createdAt: ISODate
}
```

---

## 8. Caching Strategy

| Cache Key | TTL | Invalidation |
|---|---|---|
| `tenant:{slug}:config` | 5 min | On any config save |
| `tenant:{id}:system_prompt:{schema_v}` | 24 hr | On schema or AI config change |
| `tenant:{id}:schema` | 10 min | On schema field add/edit |
| `session:{session_id}:history` | 2 hr sliding | On each new message |
| `ratelimit:tenant:{id}` | 1 min sliding window | Auto-expiry |
| `ratelimit:session:{id}` | 1 min sliding window | Auto-expiry |

All cache keys are namespaced by `tenant_id` to enforce isolation.

---

## 9. LLM Provider Adapter

```java
public interface LlmAdapter {
    Flux<String> stream(List<ChatMessage> messages, LlmConfig config);
    int countTokens(String text);
}

@Component
public class GeminiAdapter implements LlmAdapter {      // Platform default
    // Uses Spring AI VertexAiGeminiChatModel
    public Flux<String> stream(List<ChatMessage> messages, LlmConfig config) { ... }
    public int countTokens(String text) { ... }
}

@Component
public class OpenAiAdapter implements LlmAdapter {       // BYOK
    // Uses Spring AI OpenAiChatModel
    public Flux<String> stream(List<ChatMessage> messages, LlmConfig config) { ... }
    public int countTokens(String text) { ... }
}

@Component
public class LlmAdapterFactory {
    public LlmAdapter getAdapter(TenantConfig tenantConfig) {
        if ("openai".equals(tenantConfig.getByokProvider())) {
            String key = secretManager.decrypt(tenantConfig.getByokKeyEncrypted());
            return new OpenAiAdapter(key, tenantConfig.getByokModel());
        }
        return geminiAdapter; // Platform default
    }
}
```

BYOK keys decrypted **in-memory only** using GCP Secret Manager envelope encryption. Never logged.

---

## 10. System Prompt Architecture

The system prompt is compiled once and cached per tenant. Structure:

```
[ROLE]
You are {assistant_name}, the AI assistant for {business_name}.
Your knowledge is limited to the data sources connected below.

[SCHEMA]
Data fields: {schema_fields} (public fields only — admin_only excluded)
Field types and allowed values: {field_constraints}

[COMPONENTS]
You MUST respond using Component Specification JSON for all data responses.
Available components: {enabled_components}
Never invent item data. Only reference item IDs from Retrieved Items.

[ACTIONS]
Available user actions: {enabled_actions}

[PERSONA]
Tone: {tone_preset}
Language: {language}
{custom_instruction}

[GUARDRAILS]
Out-of-scope response: "{oos_message}"
Never reveal admin_only fields. Never fabricate item details.
Never output raw HTML. Always validate item IDs against Retrieved Items.

[RETRIEVED ITEMS — injected per-request, not cached]
{retrieved_items_json}
```

---

## 11. Frontend Architecture

```
apps/frontend/web/shell/src/
├── app/
│   ├── app.routes.ts                # Route config: /chat, /admin, guards
│   ├── app.component.ts             # Root shell (sidebar + topbar + router-outlet)
│   └── pages/
│       ├── chat/                    # End-user chat page
│       └── admin/                   # Admin dashboard (behind AuthGuard)
├── environments/
│   ├── environment.ts               # Dev config (builtin auth, localhost:8080)
│   └── environment.prod.ts           # Prod config (Firebase auth, Cloud Run)
└── styles.scss                      # Global M3 dark theme + design tokens

libs/frontend/
├── chat/src/lib/
│   ├── chat-window/                 # Main chat container + SSE handler
│   ├── message-list/                # Renders message history
│   ├── message-input/               # Input + starter prompts
│   ├── typing-indicator/            # 3-dot pulse animation
│   └── workflow/                    # Workflow canvas + collaboration
├── dsl-renderer/src/lib/
│   ├── dsl-renderer.component.ts    # @switch dispatcher on spec.component
│   ├── item-card/                   # 3 variants: standard, compact, featured
│   ├── item-grid/                   # Responsive 2–6 column grid
│   ├── item-detail/                 # Full detail panel
│   ├── comparison-table/            # Side-by-side 2–4 items
│   ├── filter-summary/              # Dismissible filter chips
│   ├── action-confirm/              # Confirmation before writes
│   └── empty-state/                 # Configurable no-results
├── auth/src/lib/
│   ├── auth.service.ts              # Signals-based: login, logout, token refresh
│   ├── auth.guard.ts                # CanActivate guard for admin routes
│   └── login-page/                  # Email/password + Google SSO
└── theme/src/lib/
    ├── theme.service.ts             # Load tenant theme, apply CSS vars
    ├── breakpoints.ts               # Mobile/Tablet/Desktop via CDK
    └── layout.scss                  # Grid utilities (stack, cluster, item-grid)
```

### Theme Injection (CSS Variables)
```typescript
// libs/frontend/theme/src/lib/theme.service.ts
@Injectable({ providedIn: 'root' })
export class ThemeService {
  applyTenantTheme(theme: TenantTheme): void {
    const root = document.documentElement;
    root.style.setProperty('--color-primary', theme.primaryColor);
    root.style.setProperty('--color-secondary', theme.secondaryColor);
    root.style.setProperty('--font-heading', theme.headingFont);
    root.style.setProperty('--font-body', theme.bodyFont);
  }
}
// All components use var(--color-primary) etc. — never hardcoded colors
```

---

## 12. Backend Module Structure

```
backend/
├── SynaptiqApplication.java       # Spring Boot main class
├── auth/                          # Firebase + JWT authentication module
├── tenant/                        # Tenant CRUD + lifecycle
├── datasource/                    # Data source connections, schema, records
├── integration/                   # Camel-based dynamic connectors
├── chat/                          # SSE streaming chat + LLM orchestration
├── tenantconfig/                  # AI persona, guardrails, BYOK
├── branding/                      # Theme, logo, colors
├── action/                        # Save item, contact enquiry
├── analytics/                     # Metrics, billing, usage
├── workflow/                      # Agent workflow engine
├── schemaregistry/                # Schema registry module
├── datasource/                    # Data source management
└── shared/                        # Cross-cutting config, security, events
    ├── config/                    # CORS, Jackson, security, tenant filter
    ├── domain/                    # Shared domain types
    ├── event/                     # Domain events
    ├── exception/                 # Global error handling
    └── infrastructure/            # Cross-cutting infra (web, config)
```


---

## 16. Mobile Responsiveness Strategy

### Breakpoint System

Using **Angular CDK `BreakpointObserver`** for programmatic layout switching, paired with CSS Grid for fluid layouts.

```typescript
// libs/theme/src/breakpoints.ts
export const Breakpoints = {
  Mobile:  '(max-width: 599px)',
  Tablet:  '(min-width: 600px) and (max-width: 959px)',
  Desktop: '(min-width: 960px)',
};

// Usage in component
readonly isMobile = toSignal(
  this.breakpointObserver.observe(Breakpoints.Mobile)
    .pipe(map(r => r.matches))
);
```

### Layout Adaptation Per Breakpoint

| Surface | Mobile | Tablet | Desktop |
|---|---|---|---|
| Chat window | Full screen, input pinned to bottom | Full screen | 70% width, centered max-width |
| Item grid | 1-col stacked cards | 2-col grid | 3-col grid |
| Comparison table | Horizontal scroll | 2-item comparison | Full 4-item side-by-side |
| Item detail | Full-screen bottom sheet (`mat-bottom-sheet`) | Side panel | Inline expanded card |
| Admin panel | Bottom nav + slide-over drawer | Side nav 240px | Persistent side nav 280px |
| Filter chips | Horizontal scroll row | Wrap grid | Wrap grid |

### Mobile-Specific Component Behaviour

- **Chat input**: Pinned `position: sticky; bottom: 0` — always visible above keyboard
- **Filters**: On mobile, open in `MatBottomSheet` instead of inline sidebar
- **Item detail**: `MatBottomSheet` with drag handle instead of modal dialog
- **Starter prompts**: Horizontal scroll chips — `overflow-x: auto; scroll-snap-type: x mandatory`
- **Keyboard handling**: `window.visualViewport` listener resizes chat surface when soft keyboard appears
- **Touch targets**: All interactive elements minimum `48×48px` (Material accessibility standard)

### CSS Grid Strategy

```scss
// libs/theme/src/layout.scss
.item-grid {
  display: grid;
  gap: var(--space-md);
  grid-template-columns: 1fr;                          // mobile default

  @media (min-width: 600px)  { grid-template-columns: repeat(2, 1fr); }
  @media (min-width: 960px)  { grid-template-columns: repeat(3, 1fr); }
  @media (min-width: 1280px) { grid-template-columns: repeat(4, 1fr); }
}

.chat-layout {
  display: grid;
  grid-template-rows: auto 1fr auto;  // header | messages | input
  height: 100dvh;                     // dynamic viewport height — handles mobile browser chrome
}
```

### Angular Universal (SSR) + Mobile

- SSR renders the shell with correct viewport meta tags for mobile
- `100dvh` (dynamic viewport height) used everywhere — avoids iOS Safari address bar issues
- Images use `srcset` + `loading="lazy"` for bandwidth efficiency on mobile

---

| Decision | Choice | Why Not the Alternative |
|---|---|---|
| Frontend framework | **Angular 21** (Nx) | Next.js: team expertise in Angular makes delivery faster; RxJS handles SSE natively |
| Monorepo | **Nx** | Single repo — shared domain models, API clients, design tokens across frontend + future mobile |
| Backend language | **Java 21 / Spring Boot 4** | Python: Spring offers stronger enterprise tooling, type safety, and Spring AI is now on par with LangChain |
| Primary DB | **MongoDB Atlas** | PostgreSQL: data records are dynamic documents — SQL schema fights the schema builder |
| Vector search | **Atlas Vector Search** | pgvector: Atlas VS is managed, no extension maintenance, supports per-tenant dimension variation |
| Embedding provider | **Per-tenant** (Vertex or OpenAI) | Normalized: limits BYOK flexibility; `embeddingDim` stored per document handles mixed dimensions |
| Conversation history | **Redis + MongoDB** | Redis only: 2hr TTL loses context; MongoDB: persists last 50 turns, 30-day TTL index |
| LLM structured output | **Spring AI + Jackson DTOs** | Plain prompting: too unreliable for Component DSL contract |
| Streaming protocol | **SSE** | WebSockets: overkill for unidirectional LLM streaming |
| Deployment | **Cloud Run** | GKE: over-engineered for MVP; App Engine: less flexible |
| Auth | **Single Firebase project** | Separate L0/L1 projects: unnecessary operational complexity for MVP |
| Admin UI | **Chat (dogfooding)** | Traditional dashboard: misses the opportunity to prove the product itself |
| Component hydration | **Backend hydrates** | Frontend fetches: extra round-trip + sensitive fields reach client |

---

## 14. Open Questions for Review

All questions resolved — see top of document.

## 15. Nx Monorepo Structure

```
spectrayan/
├── apps/
│   ├── shell/              # Angular 21 — end-user + admin chat (SSR via Angular Universal)
│   └── spring-apis/        # Spring Boot 4 — backend (separate Cloud Run service)
├── libs/
│   ├── domain/             # Shared TypeScript interfaces: TenantConfig, ComponentSpec, SchemaField
│   ├── ui/                 # Shared Angular component library (ChatWindow, ComponentRenderer, etc.)
│   ├── api-client/         # Auto-generated API client from OpenAPI spec
│   └── theme/              # CSS design token system, tenant theme utilities
├── nx.json
└── package.json
```

> [!NOTE]
> The Spring Boot backend lives in `apps/backend/spring-apis/` as an Nx project with a custom executor for Maven. Nx handles task dependencies: `api-client` generation runs after OpenAPI spec update, `shell` build depends on the generated SDK.
