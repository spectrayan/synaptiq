# LLM-Native Platform — Product Catalog + Discovery MVP
## Detailed Requirements

> **Core Principle**: Nothing is hardcoded. The platform is a configurable runtime engine.
> The domain (product catalog) is the first tenant *use case*, not a fixed feature.

> **Status**: Open questions resolved — see Section 13 (Resolved Decisions).

---

## Stakeholder Layers

| Layer | Actor | Role |
|---|---|---|
| **L0** | Platform Admin (you) | Manages tenants, billing, platform config |
| **L1** | Business Admin | Configures their tenant's experience end-to-end |
| **L2** | End User | Interacts with the chat interface; limited personalization |

---

## 1. Tenant Management (L0 → L1)

### 1.1 Tenant Provisioning
- **REQ-T1**: Platform admin can create a new tenant with a unique `tenant_id`, name, and subdomain (e.g., `acme.yourplatform.com`)
- **REQ-T2**: Each tenant is fully isolated — data, config, LLM context, and users are scoped to the tenant
- **REQ-T3**: Each tenant has a status: `active`, `suspended`, `trial`, `onboarding`
- **REQ-T4**: Tenant provisioning generates a default configuration with sensible defaults that the business admin can override
- **REQ-T5**: Platform admin can set per-tenant limits: max catalog items, max monthly LLM token usage, max users

### 1.2 Tenant Admin Access
- **REQ-T6**: Business admin authenticates via email/password or SSO (Google, Microsoft OAuth)
- **REQ-T7**: Business admin has a dedicated configuration dashboard (separate from the end-user chat interface)
- **REQ-T8**: Multiple admins can be invited to manage the same tenant with role-based access (Owner, Editor, Viewer)

---

## 2. Catalog Schema Configuration (L1) ← The Most Critical Section

> The platform does NOT know what a "product" is. The business defines it.

### 2.1 Schema Builder
- **REQ-S1**: Business admin can define a custom **catalog schema** — a named set of fields that describes their items (products, listings, menu items, services, etc.)
- **REQ-S2**: The catalog can be renamed by the business (e.g., "Menu", "Listings", "Products", "Services") — the word "product" is not hardcoded
- **REQ-S3**: Each schema field has:
  - `field_id` (system key, slug)
  - `label` (display name, e.g., "Price", "Spice Level", "Bedrooms")
  - `type`: one of `text`, `number`, `currency`, `boolean`, `enum`, `multi-enum`, `image_url`, `url`, `date`, `rich_text`
  - `required`: boolean
  - `searchable`: boolean (LLM can filter/query on this field)
  - `displayable`: boolean (shown in rendered components)
  - `filterable`: boolean (user can ask "show me items where X is Y")
  - `sortable`: boolean
  - `unit` (optional): e.g., "sqft", "kg", "miles"
  - `visibility`: one of `public` (visible to end users), `admin_only` (visible only in admin dashboard, never rendered in end-user components), `hidden` (exists in data, never rendered anywhere)

- **REQ-S4**: Minimum 1 field, maximum 50 fields per schema
- **REQ-S5**: One field must be designated as the **primary label** (the item's name/title)
- **REQ-S6**: One field may optionally be designated as the **primary image**
- **REQ-S7**: One field may optionally be designated as the **primary price/value**
- **REQ-S8**: Business admin can reorder fields; order determines display priority in components
- **REQ-S9**: Fields can be added, edited, or marked as `deprecated` (not deleted, to preserve existing data) at any time
- **REQ-S10**: Schema changes propagate to the LLM system prompt automatically on save
- **REQ-S11**: Fields marked `admin_only` are stripped from all end-user LLM context and component output — the LLM is never given `admin_only` field values when serving an end-user session
- **REQ-S12**: `admin_only` fields are fully accessible in the admin dashboard chat interface and admin data exports

### 2.2 Schema Examples (to validate flexibility)
The same platform must support, without code changes:
- **Restaurant**: name, price, category, description, dietary_tags (multi-enum), allergens (multi-enum), spice_level (enum), image
- **Real Estate**: address, price, bedrooms (number), bathrooms (number), sqft (number), neighborhood, property_type (enum), listing_url
- **SaaS Product**: plan_name, monthly_price, annual_price, features (multi-enum), integrations (multi-enum), support_tier (enum), cta_url
- **Auto Dealership**: make (enum), model, year (number), price, mileage (number), fuel_type (enum), color, vin, image

---

## 3. Catalog Data Management (L1)

### 3.1 Data Ingestion
- **REQ-D1**: Business admin can add catalog items manually via a form (generated dynamically from their schema)
- **REQ-D2**: Business admin can import items via **CSV upload** — column headers must map to `field_id`s; the admin completes a mapping step before import
- **REQ-D3**: Business admin can import via a **REST API webhook** — the tenant exposes an inbound API endpoint that accepts JSON payloads; the admin maps payload fields to schema fields
- **REQ-D4**: Business admin can connect a **Google Sheets** as a live data source (read-only sync, configurable sync interval: 15min / 1hr / 24hr)
- **REQ-D5**: Each catalog item has system-managed fields: `item_id`, `created_at`, `updated_at`, `status` (`active`, `draft`, `archived`)
- **REQ-D6**: Business admin can set a global item status or toggle individual items active/inactive
- **REQ-D7**: Bulk operations: activate, deactivate, delete, export selected items

### 3.2 Data Validation
- **REQ-D8**: On import, the system validates each row against the schema (required fields, type coercion, enum value membership)
- **REQ-D9**: Import errors are shown per-row with specific field-level messages; valid rows can be imported while invalid rows are flagged for review
- **REQ-D10**: The platform enforces the tenant's max catalog item limit; import that would exceed the limit fails with a clear message

---

## 4. AI Engine Configuration (L1)

### 4.1 LLM Persona
- **REQ-AI1**: Business admin can configure the assistant's **display name** (e.g., "Aria", "Max", "Acme Assistant")
- **REQ-AI2**: Business admin can configure the assistant's **personality tone** from a preset list: `Professional`, `Friendly`, `Concise`, `Enthusiastic`, `Formal`
- **REQ-AI3**: Business admin can provide a **custom system instruction** (freeform text, max 1000 chars) to further shape behavior (e.g., "Always recommend the premium option when asked for a suggestion")
- **REQ-AI4**: Business admin can set a **welcome message** shown at the start of every conversation
- **REQ-AI5**: Business admin can define up to 8 **suggested starter prompts** shown as chips in the initial chat state (e.g., "Show me options under $50", "What's your best seller?")

### 4.2 LLM Provider Configuration
- **REQ-AI-P1**: The platform provides its own managed LLM agents as the default and recommended option — marketed as the highest-quality, best-tuned experience
- **REQ-AI-P2**: Business admin can optionally configure a **Bring Your Own Key (BYOK)** model by providing an API key for a supported provider (OpenAI, Google Gemini, Anthropic Claude)
- **REQ-AI-P3**: Supported BYOK providers for MVP: OpenAI (GPT-4o, GPT-4o-mini), Google (Gemini 1.5 Pro, Gemini 1.5 Flash)
- **REQ-AI-P4**: BYOK API keys are encrypted at rest using envelope encryption; keys are never logged or exposed in plaintext
- **REQ-AI-P5**: When BYOK is active, the platform UI displays a banner in the admin dashboard: *"Using your own model — platform-optimized agents may yield better results"*
- **REQ-AI-P6**: BYOK tenants are billed only for seat charges; token charges are passed directly to their own provider account
- **REQ-AI-P7**: Platform-managed tenants are billed for both seat and token usage (see Section 10 — Pricing)
- **REQ-AI-P8**: The LLM provider interface is abstracted behind a platform-internal adapter layer — swapping providers requires no schema or config changes at the tenant level

### 4.3 LLM Behavioral Guardrails
- **REQ-AI6**: The LLM is scoped strictly to the catalog data and configured actions — it will not answer off-topic questions (applies to both platform-managed and BYOK models)
- **REQ-AI7**: If a user asks something outside scope, the LLM responds with a configurable **out-of-scope message** (default: "I'm here to help you explore [catalog name]. What are you looking for?")
- **REQ-AI8**: The LLM must not fabricate item details — all rendered data must come from the actual catalog store, not LLM memory
- **REQ-AI9**: Business admin can toggle **recommendation mode**: when enabled, the LLM proactively suggests items based on conversation context
- **REQ-AI10**: Business admin can configure **language**: the assistant responds in the configured language regardless of what language the user writes in. (Phase 2: auto-detect user language)
- **REQ-AI-MS1**: The LLM supports **multi-step intent resolution** within a single conversation turn — e.g., "Compare these 3 and save the cheapest one" is executed as a planned sequence: compare → identify cheapest → confirm → save
- **REQ-AI-MS2**: Multi-step execution is broken into visible sub-steps shown to the user as progress; the user can cancel before any write step executes
- **REQ-AI-MS3**: Each step in a multi-step sequence is individually logged in the audit trail

### 4.4 LLM Output Contract (Component DSL)
- **REQ-AI11**: The LLM never outputs raw HTML or freeform markdown for structured data — it outputs a typed **Component Specification** (JSON) that the renderer interprets
- **REQ-AI12**: Valid component types the LLM can output (see Section 6)
- **REQ-AI13**: The LLM may mix prose text with component specs in a single response (e.g., introductory text + a product card grid)
- **REQ-AI14**: If the LLM cannot determine an appropriate component, it falls back to a plain text response
- **REQ-AI15**: Component specs are validated by the platform before rendering — invalid specs are rejected silently and fall back to text
- **REQ-AI16**: The Component DSL is identical regardless of LLM provider — BYOK tenants render the same components as platform-managed tenants

---

## 5. Chat Interface (L2 — End User)

### 5.1 Core Chat Experience
- **REQ-C1**: The entire application for the end user is the chat interface — there is no other screen or navigation
- **REQ-C2**: Chat supports text input; voice input is a Phase 2 feature
- **REQ-C3**: The chat displays a history of the conversation in the current session
- **REQ-C4**: Each message can contain: plain text, a single component, or a sequence of components
- **REQ-C5**: The chat input has a configurable **placeholder text** set by the business admin
- **REQ-C6**: Suggested starter prompts appear as tappable chips above the input field when the conversation is new/empty
- **REQ-C7**: A typing indicator ("...") is shown while the LLM is generating a response
- **REQ-C8**: If the LLM takes > 10 seconds, a configurable wait message is shown ("Just a moment...")
- **REQ-C9**: The user can clear/reset the conversation at any time
- **REQ-C10**: Conversation history is maintained within a browser session; persistence across sessions is a Phase 2 feature

### 5.2 End User Personalization
> Business admin controls the *range* of what users can personalize

- **REQ-C11**: Business admin can enable/disable each personalization option for their end users
- **REQ-C12**: If enabled by business admin, end user can switch **color theme**: choose from a set of themes the business admin has pre-approved (min 1, max 5)
- **REQ-C13**: If enabled by business admin, end user can switch **font**: choose from a list the business admin selects (min 1, max 3 Google Fonts)
- **REQ-C14**: If enabled by business admin, end user can switch **chat bubble style**: `modern` (rounded), `minimal` (flat), `compact` (dense)
- **REQ-C15**: End user preferences are persisted in `localStorage`
- **REQ-C16**: End user cannot change anything outside the business-admin-approved personalization set

---

## 6. Component Library (The UI Grammar)

> These are the pre-built, configurable UI components the LLM can choose to render.
> Business admin controls which components are enabled for their tenant.

### 6.1 Component: Item Card (`item-card`)
- Renders a single catalog item
- Fields displayed: configurable by business admin (select which schema fields to show, in what order)
- Supports: image, title, subtitle, badges (from enum fields), price, action buttons
- Variants: `default`, `compact`, `featured`

### 6.2 Component: Item Card Grid (`item-grid`)
- Renders 2–6 items in a responsive card grid
- Inherits `item-card` field configuration
- Business admin sets max items per grid (2, 3, or 4)

### 6.3 Component: Item Detail (`item-detail`)
- Full-detail view of a single item — all displayable fields
- Triggered when user asks for more info on a specific item
- Includes all action buttons configured for this tenant

### 6.4 Component: Comparison Table (`comparison-table`)
- Side-by-side comparison of 2–4 items
- Rows = fields; columns = items
- Business admin selects which fields appear in comparisons

### 6.5 Component: Filter Summary (`filter-summary`)
- Shows currently active filters as dismissible chips
- E.g., "Category: Electronics × | Price: Under $100 ×"
- Appears above item grids when filters are active

### 6.6 Component: Result Count Banner (`result-count`)
- Shows "Showing X of Y items matching your criteria"
- Configurable show/hide by business admin

### 6.7 Component: Empty State (`empty-state`)
- Shown when no items match — with a configurable message and a suggested action
- Business admin can set the empty state message and suggestion text

### 6.8 Component: Action Confirmation (`action-confirm`)
- Shown before executing any write action (see Section 7)
- Not disableable — always required for actions

### 6.9 Component: Info Banner (`info-banner`)
- Inline informational text block styled distinctly from assistant prose
- Used for policies, notes, disclaimers configured by the business

### 6.10 Component Enablement
- **REQ-CM1**: Business admin can enable or disable each component type for their tenant
- **REQ-CM2**: Disabled components are never rendered; the LLM is instructed not to use them
- **REQ-CM3**: Business admin configures field visibility per component independently

---

## 7. Actions Configuration (L1)

> Actions are things the end user can trigger from the chat. All are opt-in.

### 7.1 Available Actions (Phase 1)
| Action ID | Description | Configurable |
|---|---|---|
| `save_item` | Save/bookmark an item — persisted to database per user session | Toggle on/off; label customizable |
| `share_item` | Copy a shareable link to a specific item | Toggle on/off |
| `contact_enquiry` | Open a pre-filled contact/enquiry form in chat | Toggle on/off; form fields configurable |
| `view_external` | Open an external URL (from a URL-type field) | Toggle on/off; button label customizable |
| `show_more` | Load more details about an item | Always on |

### 7.2 Action Configuration
- **REQ-A1**: Business admin can enable/disable each action globally for the tenant
- **REQ-A2**: Business admin can customize the **label** for each action button (e.g., "Save" vs "Wishlist" vs "Shortlist")
- **REQ-A3**: All write actions (anything except `show_more`, `share_item`, `view_external`) must show an `action-confirm` component before executing
- **REQ-A4**: `contact_enquiry` form fields are fully configurable: add/remove fields, mark required, set field types (text, email, phone, select)
- **REQ-A5**: `contact_enquiry` submission destination is configurable: email address, webhook URL, or both
- **REQ-A6**: Actions are rendered as buttons within components, not as chat messages from the user
- **REQ-A7**: `save_item` persists to the platform database — associated to a `session_id` for anonymous users; associated to `user_id` when authenticated (Phase 2)
- **REQ-A8**: Saved items are retrievable within the chat (e.g., "Show me my saved items") — the LLM can render them as an `item-grid` component
- **REQ-A9**: Saved items are scoped to the tenant — a user's saved items in Tenant A are not visible in Tenant B
- **REQ-A10**: Saved item records include: `item_id`, `tenant_id`, `session_id`, `saved_at`, `item_snapshot` (copy of item data at save time to handle future catalog changes)

### 7.3 Action Execution Layer
- **REQ-A-DET1**: All action execution is **deterministic** — identical inputs and permissions always produce identical outcomes with no LLM non-determinism in the execution path
- **REQ-A-DET2**: Every action execution is **permission-checked** at runtime against the tenant's configured action set and the user's session role before executing
- **REQ-A-DET3**: Every action execution is **fully logged** — `action_id`, `session_id`, `tenant_id`, `timestamp`, `input_snapshot`, `outcome` (success/failure), `error` if any
- **REQ-A-DET4**: Transient failures (network errors, timeout) are retried up to 3 times with exponential backoff before returning a failure response to the user
- **REQ-A-DET5**: Action logs are append-only and immutable; they cannot be modified or deleted by the business admin

---

## 8. Branding & Theming (L1)

### 8.1 Business Branding
- **REQ-B1**: Business admin can upload a **logo** (PNG/SVG, max 500KB) displayed in the chat header
- **REQ-B2**: Business admin can set a **primary color** (hex picker) — used for CTAs, highlights, and active states
- **REQ-B3**: Business admin can set a **secondary color** (hex picker)
- **REQ-B4**: Business admin can set a **background style**: `light`, `dark`, or `auto` (follows system preference)
- **REQ-B5**: Business admin selects up to **3 fonts** from a curated list of Google Fonts (platform provides a pre-screened list of ~20 fonts) — one for headings, one for body, one for UI labels
- **REQ-B6**: Business admin can create up to 5 **named themes** (combination of colors + font set) to offer end users
- **REQ-B7**: One theme must be designated as the **default**
- **REQ-B8**: Business admin can set the **favicon** for their hosted subdomain
- **REQ-B9**: Business admin can set a **custom page title** for their hosted interface (shown in browser tab)

### 8.2 Theme System Constraints
- **REQ-B10**: The platform enforces minimum contrast ratios (WCAG AA 4.5:1) — if the admin's color choices fail contrast, the system warns and prevents save
- **REQ-B11**: Platform branding (your logo/name) is shown in a configurable footer — can be shown or hidden based on tenant plan tier
- **REQ-B12**: All theme values are stored as design tokens; components reference tokens, not raw values

---

## 9. Hosting & Access (L1)

- **REQ-H1**: Each tenant's chat interface is accessible at `{tenant_slug}.{platform_domain}.com` — this is the **primary and only hosting mode for MVP**
- **REQ-H2**: Business admin can configure a **custom domain** (CNAME delegation) — Phase 2 feature
- **REQ-H3**: Business admin can set access mode:
  - `public`: anyone with the link can access
  - `unlisted`: link-only (no login required, but not indexed)
  - `protected`: end users must provide a password (single shared password) — Phase 2
  - `authenticated`: end users must log in — Phase 2
- **REQ-H4**: Website embed script (iframe) — Phase 2 feature
- **REQ-H5**: The hosted subdomain supports `?theme=dark` and `?lang=fr` URL parameters to override tenant defaults

---

## 10. Analytics & Observability (L1)

### 10.1 Business Admin Dashboard — Chat-Paradigm Interface

> The business admin dashboard is itself built using the platform's chat paradigm — dogfooding the product. Admins interact with a chat interface to configure their tenant, with the LLM guiding the setup and surfacing configuration components inline.

- **REQ-AN0**: The admin dashboard IS a chat interface — configuration panels, metrics, and management tools are rendered as components within the chat, exactly as end-user catalog components are
- **REQ-AN0a**: The admin assistant is pre-configured by the platform (not customizable by the tenant) with a setup/management persona
- **REQ-AN0b**: Admin chat examples: *"Add a new product"* → renders an inline schema form; *"Show me this week's analytics"* → renders a metrics dashboard component; *"Change my primary color to blue"* → renders a color picker component
- **REQ-AN0c**: Traditional form fallbacks are available for all config operations for users who prefer direct input over chat

### 10.2 Analytics & Metrics
- **REQ-AN1**: Total conversations (daily / weekly / monthly)
- **REQ-AN2**: Total messages sent by end users
- **REQ-AN3**: Most queried items (which catalog items appeared most in responses)
- **REQ-AN4**: Most common search intents (top 10 query themes, LLM-summarized)
- **REQ-AN5**: Action conversion rates (how often each action was triggered vs. conversations)
- **REQ-AN6**: Zero-result queries (queries where no matching items were found — helps admin improve catalog)
- **REQ-AN7**: LLM token usage (current period vs. plan limit with breakdown: platform-managed vs BYOK)
- **REQ-AN8**: Seat count (active users this billing period)
- **REQ-AN9**: Metrics rendered as dashboard components within the admin chat interface; date range filter configurable

### 10.3 Pricing Instrumentation
- **REQ-PR1**: The platform charges on a **combined seat + token model**
- **REQ-PR2**: **Seat charge**: billed per unique end-user session per calendar month (a `session_id` counts as one seat)
- **REQ-PR3**: **Token charge**: billed per 1,000 LLM tokens consumed (input + output) for platform-managed tenants; BYOK tenants pay $0 for tokens
- **REQ-PR4**: Each tenant has a configurable monthly seat cap and token cap; exceeding either triggers a configurable action: `warn_admin`, `throttle`, or `block`
- **REQ-PR5**: Usage events (seat starts, token consumption) are written to an append-only usage ledger in real time
- **REQ-PR6**: Billing reports are available to the business admin (current period spend, breakdown by seat vs token)
- **REQ-PR7**: Platform admin (L0) sees cost rollup across all tenants for margin management

### 10.4 Platform Admin Metrics (L0)
- **REQ-AN10**: Tenant-level usage rollup: active tenants, total conversations, total token spend
- **REQ-AN11**: Cost-per-tenant reporting: revenue vs. LLM cost per tenant for margin visibility
- **REQ-AN12**: BYOK vs platform-managed tenant split and trend

---

## 11. Non-Functional Requirements

### 11.1 Performance
- **REQ-NF1**: Chat interface initial load < 2 seconds (P95)
- **REQ-NF2**: LLM first token response < 2 seconds (P95) — streaming required
- **REQ-NF3**: Component render after LLM response < 100ms
- **REQ-NF4**: Catalog search/retrieval < 500ms (P95)
- **REQ-NF4a**: Cached catalog embeddings (warm cache) must return in < 50ms

### 11.2 Scalability
- **REQ-NF5**: Platform must support up to 100 tenants in MVP phase without architecture changes
- **REQ-NF6**: Each tenant catalog supports up to 10,000 items in MVP
- **REQ-NF7**: Concurrent users: up to 50 simultaneous conversations per tenant
- **REQ-NF-SL1**: All platform services are **stateless** — no session state is held in application memory; all state is persisted in the database or cache layer
- **REQ-NF-SL2**: Stateless design enables horizontal scaling by adding service instances without coordination
- **REQ-NF-CACHE1**: The LLM system prompt (compiled from tenant config + schema) is **cached per tenant** and invalidated only on schema or config change
- **REQ-NF-CACHE2**: Catalog item embedding vectors are cached in a vector store; the cache is incrementally updated on item add/edit/delete, not rebuilt in full

### 11.3 Reliability
- **REQ-NF8**: Platform uptime SLA: **99.9%** (~8.7 hours downtime/year)
- **REQ-NF9**: If the LLM API is unavailable, the chat shows a graceful error; catalog search falls back to keyword search
- **REQ-NF10**: All tenant configuration is persisted durably; a config save must be atomic (all-or-nothing)
- **REQ-NF-CB1**: A **circuit breaker** wraps all LLM provider calls — if the error rate exceeds 50% over a 30-second window, the circuit opens and requests fail fast with graceful degradation (keyword fallback)
- **REQ-NF-CB2**: The circuit breaker resets automatically after a configurable cooldown period (default: 60 seconds)
- **REQ-NF-CB3**: Platform admin is alerted when a tenant's LLM circuit breaker opens

### 11.4 Security
- **REQ-NF11**: All data is encrypted in transit (TLS 1.2+) and at rest
- **REQ-NF12**: Tenant data is logically isolated — no cross-tenant data leakage
- **REQ-NF13**: LLM prompts include the tenant's catalog data only — no other tenant's data enters the context
- **REQ-NF14**: Admin authentication tokens expire after 24 hours of inactivity
- **REQ-NF15**: All admin configuration changes are logged with timestamp and actor
- **REQ-NF16**: BYOK API keys are stored encrypted using envelope encryption (AES-256); the plaintext key is never logged, never returned via API, and is only decrypted in-memory at LLM call time
- **REQ-NF-ZT1**: **Zero-trust architecture** — all internal service-to-service calls require authentication; no service is implicitly trusted by another regardless of network location
- **REQ-NF-ZT2**: Each internal service uses short-lived signed tokens (JWT, max 15 min TTL) for inter-service authentication
- **REQ-NF-RL1**: **Rate limiting** is enforced per-tenant: configurable max LLM requests per minute (default: 60 req/min per tenant)
- **REQ-NF-RL2**: Rate limiting is enforced per-session: max 10 LLM requests per minute per end-user session to prevent individual abuse
- **REQ-NF-RL3**: Rate limit breaches return a graceful in-chat message; the session is not terminated
- **REQ-NF-RL4**: Rate limit events are logged and visible to the platform admin in tenant usage reports

### 11.5 Accessibility
- **REQ-NF16**: Chat interface meets WCAG 2.1 AA
- **REQ-NF17**: All interactive elements are keyboard navigable
- **REQ-NF18**: Screen reader compatible (ARIA labels on all components)

### 11.6 Internationalization
- **REQ-NF19**: Platform admin UI supports English (MVP)
- **REQ-NF20**: Chat interface supports any language configured by the business admin
- **REQ-NF21**: All UI strings in the chat interface are configurable (no hardcoded English copy)

---

## 12. Out of Scope (MVP)

### Phase 2 Deferrals
- End user authentication / persistent accounts (MVP uses anonymous session_id)
- Custom domain (CNAME) support
- Website embed script (iframe)
- Voice input
- Payment / checkout integration
- Multi-language auto-detection (admin sets one language; auto-detect is Phase 2)
- Mobile native app (MVP is web-only, mobile-responsive)
- Inventory / stock management
- User-to-user features
- A/B testing of LLM responses
- Offline mode
- SAML SSO for enterprise tenants (OAuth only in MVP)
- Full SQL/NoSQL data source connectors (Postgres, MySQL, MongoDB, Snowflake)
- SaaS API connectors (Salesforce, HubSpot, Zendesk)
- AI-generated workflow rules engine
- Dashboard canvas (multi-component pinned layout)
- Chart components (bar, line, pie, heatmap) — critical for analytics vertical
- General-purpose AI-generated forms (beyond `contact_enquiry`)
- Notifications and scheduled job triggers

### Future Roadmap (Post Phase 2)
- SOC2 Type II compliance *(target: 12 months post-launch — must be designed for from day one)*
- Multi-agent collaboration
- Predictive analytics
- Plugin / integration marketplace
- AI-generated onboarding flows
- Event stream connectors (Kafka, Kinesis)
- Business vocabulary mapping (custom metric/KPI definitions)

---

## 13. Resolved Decisions

| # | Decision | Resolution |
|---|---|---|
| D1 | LLM Provider Model | Platform-managed agents are default + recommended. BYOK supported (OpenAI, Gemini). Provider abstracted behind adapter layer. |
| D2 | Saved Items Persistence | Saved items persist to the **database** (not session/localStorage), keyed by `session_id` for anonymous users. |
| D3 | Search Backend | **Vector search** (semantic) — enables natural language queries like "cozy winter options" to match contextually. |
| D4 | Pricing Model | **Seat + Token** combined. BYOK tenants pay seat-only. Platform-managed tenants pay seat + token. Append-only usage ledger from day one. |
| D5 | Admin Dashboard | Built using the **same chat paradigm** as the end-user interface — dogfooding the product. Config panels rendered as inline components. |
| D6 | Hosting Model | **Hosted subdomain only** for MVP (`acme.yourplatform.com`). Embed script deferred to Phase 2. |
