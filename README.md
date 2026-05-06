# Synaptiq

**AI-native, chat-first B2B SaaS platform for product catalog discovery.**

Synaptiq lets businesses embed an intelligent, conversational catalog assistant into any digital channel. Tenants import their product catalog, configure a branded AI persona, and get a chat widget that can search, compare, filter, and recommend items — all powered by LLM + vector search.

> **Status:** MVP in progress — **116 / 131 tasks complete** across 14 phases.

---

## ✨ Key Features

| Area | Highlights |
|---|---|
| **Multi-tenant architecture** | Subdomain-based tenant isolation, role-based access (platform admin → tenant viewer), per-tenant rate limiting |
| **Catalog management** | Schema import (OpenAPI / JSON Schema / YAML), CSV bulk import, field-level search / filter / sort control |
| **AI chat engine** | Streaming SSE responses, Gemini & OpenAI adapters, BYOK support, circuit breaker + keyword fallback |
| **Vector search** | MongoDB Atlas Vector Search with Gemini `text-embedding-004`, automatic re-embedding on schema changes |
| **Component DSL** | 10 rich UI components (item cards, grids, comparison tables, filters, etc.) rendered inline in chat |
| **Actions engine** | Save items, contact enquiries, audit-logged with retry + exponential backoff |
| **Branding & theming** | Per-tenant logos, color palettes, fonts, named themes (max 5), WCAG AA contrast validation |
| **Admin dashboard** | Chat-paradigm admin — manage config, schema, branding, and analytics via inline DSL components |
| **Analytics** | Conversation metrics, token usage vs plan limits, billing reports, platform-wide rollups |

---

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| **Frontend** | Angular 21 (zoneless, signals, SSR) + Angular Material 3 |
| **Backend** | Python 3.12+ / FastAPI (modular monolith) |
| **AI / LLM** | Google Gemini (default) · OpenAI (BYOK) · LangChain · Instructor |
| **Database** | MongoDB Atlas + Vector Search (via Motor async driver) |
| **Cache & Rate Limiting** | Redis 7 |
| **Auth** | Firebase Auth (multi-tenant, custom claims) |
| **Deployment** | Google Cloud Run (planned) |
| **Monorepo** | Nx 22 |
| **Package Manager** | npm (frontend) · uv (backend) |

---

## 📁 Project Structure

```
synaptiq/
├── apps/
│   ├── frontend/web/shell/            # Angular 21 SSR shell app
│   └── backend/api/                   # FastAPI service
│       └── src/synaptiq_api/
│           ├── routers/               # API route handlers
│           │   ├── auth.py            #   Firebase auth endpoints
│           │   ├── tenants.py         #   Tenant CRUD + admin invite
│           │   ├── catalog.py         #   Schema import, item CRUD, CSV
│           │   ├── chat.py            #   SSE chat streaming
│           │   ├── config.py          #   AI persona, guardrails, BYOK
│           │   ├── branding.py        #   Theme, logo, colors
│           │   ├── actions.py         #   Save item, contact enquiry
│           │   ├── analytics.py       #   Metrics, billing, usage
│           │   └── health.py          #   Liveness + readiness probes
│           ├── services/              # Business logic
│           │   ├── chat_service.py    #   Orchestrates LLM + search + DSL
│           │   ├── llm_provider.py    #   Gemini / OpenAI adapters + circuit breaker
│           │   ├── embedding_service.py
│           │   ├── search_service.py  #   Vector search + metadata filters
│           │   ├── prompt_service.py  #   System prompt builder + Redis cache
│           │   ├── catalog_service.py
│           │   ├── schema_service.py
│           │   ├── schema_parser.py   #   OpenAPI / JSON Schema / YAML parser
│           │   ├── tenant_service.py
│           │   ├── auth_service.py
│           │   └── action_service.py
│           ├── middleware/            # Auth, tenant resolution, rate limiting
│           ├── models/               # Pydantic domain models
│           ├── core/                 # Config, DB, dependencies
│           └── scripts/              # Seed data, migrations
├── libs/
│   ├── frontend/
│   │   ├── auth/                     # AuthService, AuthGuard, login page
│   │   ├── chat/                     # Chat UI — message list, input, streaming
│   │   ├── dsl-renderer/            # 10 DSL component renderers
│   │   └── theme/                   # M3 theme service + CSS var injection
│   └── shared/
│       ├── constants/               # DSL ComponentSpec types, enums
│       └── utils/                   # Shared utilities
├── docs/
│   ├── requirements.md              # Full requirements spec
│   ├── architecture.md              # System architecture
│   ├── tasks.md                     # Phase-by-phase task tracker
│   └── form_architecture.md         # Admin form component design
├── docker-compose.yml               # MongoDB + Redis + Firebase Auth Emulator + API
└── .agents/                         # Agent skills & workflows
```

---

## 🚀 Quick Start

### Prerequisites

- **Node.js** 22+
- **Python** 3.12+
- **uv** package manager (`pip install uv`)
- **Docker** + Docker Compose

### 1. Clone & install Node dependencies

```bash
git clone <repo-url> && cd synaptiq
npm install
```

### 2. Start local services

```bash
# MongoDB Atlas Local (vector search) + Redis + Firebase Auth Emulator
docker compose up mongodb redis firebase-auth -d
```

### 3. Start the API

```bash
cd apps/backend/api
cp .env.example .env        # configure env vars (see below)
uv sync
uv run uvicorn synaptiq_api.main:app --reload --port 8080
```

### 4. Seed demo data

```bash
# In a separate terminal (requires MongoDB + Redis running)
cd apps/backend/api
uv run python -m synaptiq_api.scripts.seed_dev
```

### 5. Start the Angular shell

```bash
npx nx serve shell
```

Open **http://localhost:4200**

---

## ⚙️ Environment Variables

The API reads configuration from `apps/backend/api/.env`:

| Variable | Description | Default |
|---|---|---|
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017` |
| `REDIS_URL` | Redis connection URL | `redis://localhost:6379` |
| `FIREBASE_PROJECT_ID` | Firebase project ID | `synaptiq-dev` |
| `FIREBASE_AUTH_EMULATOR_HOST` | Auth emulator (dev only) | `localhost:9099` |
| `GEMINI_API_KEY` | Google Gemini API key | — |
| `OPENAI_API_KEY` | OpenAI API key (optional, BYOK) | — |
| `SYNAPTIQ_CORS_ORIGINS` | Allowed CORS origins | `http://localhost:4200` |
| `ENVIRONMENT` | `development` / `production` | `development` |
| `DEBUG` | Enable debug logging | `true` |

---

## 🧰 Nx Commands

```bash
# Serve the Angular shell (dev mode)
npx nx serve shell

# Build for production
npx nx build shell

# Run all tests
npx nx run-many -t test

# Lint everything
npx nx run-many -t lint

# Generate a new Angular component
NX_IGNORE_UNSUPPORTED_TS_SETUP=true npx nx g @nx/angular:component \
  --name=my-component --project=shell
```

---

## 🧪 API Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/auth/login` | Exchange Firebase token for session |
| `POST` | `/tenants` | Create tenant (platform admin) |
| `GET/PATCH` | `/tenants/{id}` | Manage tenant config & limits |
| `POST` | `/catalog/schema/import` | Import schema (OpenAPI / JSON / YAML) |
| `GET/PATCH` | `/catalog/schema` | View / annotate catalog schema |
| `POST` | `/catalog/items` | Create catalog item |
| `GET` | `/catalog/items` | List items (paginated, filtered) |
| `POST` | `/catalog/import/csv` | Bulk CSV import |
| `POST` | `/chat/message` | SSE streaming chat (main endpoint) |
| `POST/DELETE` | `/sessions` | Session lifecycle |
| `GET/PATCH` | `/config/ai` | AI persona config |
| `GET/PATCH` | `/config/ai/provider` | LLM provider / BYOK |
| `GET/PATCH` | `/config/ai/guardrails` | AI guardrails |
| `GET/PATCH` | `/config/components` | DSL component toggles |
| `GET/PATCH` | `/config/actions` | Action enablement |
| `GET/PATCH` | `/config/branding` | Theme, colors, logo |
| `POST` | `/actions/save_item` | Save item to session |
| `POST` | `/actions/contact_enquiry` | Submit enquiry form |
| `GET` | `/analytics/summary` | Aggregated usage metrics |
| `GET` | `/health/live` | Liveness probe |

---

## 📊 Progress

| Phase | Area | Tasks | Status |
|---|---|---|---|
| 0 | Foundation / Scaffolding | 12 | ✅ Done |
| 1 | Infrastructure & Auth | 14 | 🔶 13/14 |
| 2 | Tenant Management API | 6 | ✅ Done |
| 3 | Catalog Schema (Import-First) | 7 | ✅ Done |
| 4 | Catalog Data Management | 7 | ✅ Done |
| 5 | Vector Search & System Prompt | 7 | ✅ Done |
| 6 | AI Engine & Chat API | 14 | ✅ Done |
| 7 | Component DSL & Renderer | 13 | ✅ Done |
| 8 | Actions Engine | 6 | ✅ Done |
| 9 | AI Config API & UI | 6 | ✅ Done |
| 10 | Branding & Theming | 9 | ✅ Done |
| 11 | Admin Dashboard | 7 | 🔶 5/7 |
| 12 | Analytics & Usage | 6 | 🔶 5/6 |
| 13 | Chat UI Polish | 7 | 🔶 5/7 |
| 14 | DevOps & NFRs | 10 | ⬜ Not started |
| **Total** | | **131** | **116 done (89%)** |

See [tasks.md](./docs/tasks.md) for the full checklist.

---

## 📚 Documentation

- [Requirements](./docs/requirements.md) — Full functional & non-functional spec
- [Architecture](./docs/architecture.md) — System architecture & data flow
- [Task Tracker](./docs/tasks.md) — Phase-by-phase implementation checklist
- [Form Architecture](./docs/form_architecture.md) — Admin form component design

---

## 📄 License

MIT
