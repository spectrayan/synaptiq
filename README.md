# Synaptiq

AI-native, chat-first B2B SaaS platform for product catalog discovery.

## Stack

| Layer | Technology |
|---|---|
| Frontend | Angular 21 (zoneless, signals) + Angular Material 3 |
| Backend | Python FastAPI (modular monolith) |
| Database | MongoDB Atlas + Vector Search |
| Cache / Sessions | Upstash Redis |
| Auth | Firebase Auth (multi-tenant) |
| Deployment | Google Cloud Run |
| Monorepo | Nx 22 |

## Structure

```
synaptiq/
├── apps/
│   ├── frontend/web/shell/      # Angular 21 SSR app
│   └── backend/api/             # FastAPI service
├── libs/
│   ├── frontend/
│   │   ├── ui/                  # Shared UI components
│   │   ├── chat/                # Chat UI lib
│   │   ├── dsl-renderer/        # Component DSL renderer
│   │   └── theme/               # M3 theme utilities
│   └── shared/
│       ├── types/               # TS domain models
│       ├── utils/               # Shared utilities
│       ├── constants/           # Shared constants
│       └── sdks/
│           ├── angular-sdk/     # Generated API client
│           └── python-sdk/      # Shared Python types
├── docs/                        # Architecture & requirements
├── docker-compose.yml           # Local dev stack
└── .agents/                     # Agent skills & workflows
```

## Quick Start

### Prerequisites
- Node 22+
- Python 3.12+
- `uv` package manager (`pip install uv`)
- Docker + Docker Compose

### 1. Install Node dependencies
```bash
npm install
```

### 2. Start local services (MongoDB + Redis)
```bash
docker-compose up mongodb redis -d
```

### 3. Start the API
```bash
cd apps/backend/api
uv sync
uv run uvicorn synaptiq_api.main:app --reload
```

### 4. Start the Angular shell
```bash
npx nx serve shell
```

Open `http://localhost:4200`

## Nx Commands

```bash
# Serve the Angular shell
npx nx serve shell

# Build shell for production
npx nx build shell

# Run all tests
npx nx run-many -t test

# Lint everything
npx nx run-many -t lint

# Generate a new Angular component
NX_IGNORE_UNSUPPORTED_TS_SETUP=true npx nx g @nx/angular:component --name=my-component --project=shell
```

## Docs

- [Requirements](./docs/requirements.md)
- [Architecture](./docs/architecture.md)
