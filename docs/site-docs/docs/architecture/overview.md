# Architecture Overview

Synaptiq is built as a **modular monolith** using Spring Modulith — combining the simplicity of a single deployment unit with the module isolation of microservices.

---

## System Architecture

```mermaid
graph TB
    subgraph Frontend["Angular 21 — Dynamic UI Renderer"]
        UI["Chat Shell<br/>Signals · SSR"]
        DSL["Component DSL Renderer<br/>20+ component types"]
        WFD["Workflow Designer<br/>Visual flow editor"]
        Theme["Theme Engine<br/>M3 · Branding · CSS vars"]
    end

    subgraph Backend["Spring Boot 4.0 — Java 21, WebFlux"]
        API["REST API Layer<br/>OpenAPI Contract-First"]
        Auth["Spring Security<br/>JWT · Firebase · RBAC"]
        MCP["MCP Server<br/>AI tool exposure"]

        subgraph Modules["Spring Modulith Modules"]
            CH["Chat Engine<br/>LLM → Component DSL"]
            WF["Workflow Engine<br/>Multi-agent orchestration"]
            KB["Knowledge Base<br/>RAG pipeline"]
            SR["Schema Registry<br/>Semantic data model"]
            TC["Tenant Config<br/>Persona · Guardrails"]
            BR["Branding<br/>Themes · Colors"]
            AC["Actions<br/>CRUD · Enquiries"]
            INT["Integrations<br/>Apache Camel"]
        end

        SpringAI["Spring AI<br/>Prompt assembly · Tool calling"]
    end

    subgraph Data["Data Layer"]
        Mongo[("MongoDB Atlas<br/>+ Vector Search")]
    end

    subgraph LLMs["LLM Providers"]
        Gemini["Google Gemini"]
        OpenAI["OpenAI"]
        Ollama["Ollama"]
    end

    UI --> API
    DSL --> UI
    WFD --> UI
    Theme --> UI
    API --> Auth --> Modules
    CH & WF --> SpringAI
    SpringAI --> LLMs
    Modules --> Mongo
    KB --> Mongo
    MCP --> SpringAI
```

---

## Design Principles

| Principle | Implementation |
|-----------|---------------|
| **AI generates the UI** | LLM emits declarative Component DSL JSON; frontend renders natively |
| **Secure by design** | Backend hydration — LLM never sees sensitive data |
| **API-First** | OpenAPI spec → generated Java interfaces + Angular SDK |
| **Hexagonal Architecture** | Domain core is pure POJOs — no framework annotations |
| **DDD Bounded Contexts** | Each module owns its aggregate root and domain events |
| **Event-Driven** | Modules communicate via `@ApplicationModuleListener` events only |
| **Reactive End-to-End** | WebFlux + Reactive MongoDB for non-blocking I/O |
| **RFC 9457 Errors** | Standardized `application/problem+json` responses |
| **Contract-First SDK** | Single OpenAPI spec generates Java server + Angular + Kotlin + Swift clients |

---

## Module Dependency Map

```mermaid
graph LR
    subgraph Core["Core Modules"]
        Auth["Auth & RBAC"]
        Config["Tenant Config"]
    end

    subgraph Domain["Domain Modules"]
        Chat["Chat Engine"]
        Workflow["Workflow Engine"]
        KB["Knowledge Base"]
        Schema["Schema Registry"]
        Branding["Branding"]
        Actions["Actions"]
    end

    subgraph Cross["Cross-Cutting"]
        Shared["Shared Config"]
        Integration["Integrations"]
    end

    Chat --> Auth
    Chat --> Config
    Chat --> KB
    Workflow --> Auth
    Workflow --> Config
    KB --> Auth
    Schema --> Auth
    Branding --> Config
    Actions --> Auth
    Integration --> Auth
    
    style Auth fill:#E3F2FD,color:#333,stroke:#1565C0
    style Config fill:#E3F2FD,color:#333,stroke:#1565C0
    style Chat fill:#E8F5E9,color:#333,stroke:#2E7D32
    style Workflow fill:#E8F5E9,color:#333,stroke:#2E7D32
    style KB fill:#E8F5E9,color:#333,stroke:#2E7D32
    style Schema fill:#E8F5E9,color:#333,stroke:#2E7D32
```

---

## Data Flow

### Chat Request Flow

```mermaid
sequenceDiagram
    participant Client as Angular Client
    participant Filter as JWT Filter
    participant Controller as ChatController
    participant Service as ChatMessageService
    participant RAG as VectorStore
    participant LLM as Spring AI (Gemini)
    participant DB as MongoDB

    Client->>Filter: POST /api/v1/chat/message<br/>(Bearer JWT)
    Filter->>Controller: Authenticated request
    Controller->>Service: processMessage(sessionId, content)
    Service->>RAG: retrieveRagContext(query)
    RAG->>DB: Vector similarity search
    DB-->>RAG: Top-K relevant chunks
    RAG-->>Service: Context documents
    Service->>LLM: System prompt + schema + RAG context + query
    LLM-->>Service: SSE stream (text + Component DSL)
    Service->>DB: Persist conversation turn
    Service-->>Controller: Flux<ServerSentEvent>
    Controller-->>Client: SSE stream
```

---

## Monorepo Structure

```
synaptiq/                              # Nx 22 monorepo root
├── apps/
│   ├── frontend/web/shell/            # Angular 21 — chat shell + DSL renderer
│   └── backend/spring-apis/           # Spring Boot 4 (WebFlux + Modulith)
│       └── src/main/java/com/spectrayan/synaptiq/
│           ├── chat/                  #   Chat engine module
│           ├── workflow/              #   Multi-agent workflow engine
│           ├── knowledgebase/         #   Knowledge base + RAG
│           ├── schemaregistry/        #   Schema registry
│           ├── tenantconfig/          #   AI persona, guardrails
│           ├── branding/              #   Theme, logo, colors
│           ├── auth/                  #   Authentication + RBAC
│           ├── integration/           #   Apache Camel integrations
│           └── shared/                #   Cross-cutting config, security
├── libs/
│   ├── frontend/
│   │   ├── dsl-renderer/             # 20+ DSL component renderers
│   │   ├── auth/                     # AuthService, AuthGuard, login
│   │   ├── chat/                     # Chat UI — message list, input
│   │   └── theme/                    # M3 theme service + CSS vars
│   ├── backend/
│   │   └── agent-flow-spring/        # Spring-based multi-agent engine
│   └── shared/
│       ├── openapi-spec/             # OpenAPI 3.0 contract
│       ├── sdks/                     # Generated SDKs (Angular, Kotlin, Swift)
│       ├── apis/                     # Generated Spring server stubs
│       └── constants/                # Component DSL type definitions
├── docs/
│   ├── site-docs/                    # MkDocs documentation site
│   ├── architecture.md               # Detailed architecture doc
│   └── vision.md                     # Platform vision & strategy
├── seed-data/                        # Database seeding scripts
├── scripts/                          # Start/stop/seed scripts
└── docker-compose.yml                # MongoDB infrastructure
```
