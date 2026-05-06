# Current Focus: Synaptiq Java Spring Boot Backend (spring-apis)

## Architecture
- **Hexagonal/DDD** — strict Ports & Adapters, matching Promptly reference
- **Spring Boot 4.0.0** + **Spring WebFlux** (reactive)
- **Spring Modulith 2.1.0-M4** (module boundaries via package-info.java)
- **Spring AI 2.0.0-M4** (Vertex AI Gemini)
- **API-first** (SpringDoc OpenAPI 3.0.3)
- **MongoDB Reactive** + **Redis**

## Key Principles Enforced
- Domain models are **pure POJOs** — NO @Document, @Id, or framework annotations
- Controllers depend **ONLY** on input port interfaces (use cases)
- MongoDB documents live in `infrastructure/persistence/mongo/entity/`
- Mappers (MapStruct or manual) convert domain ↔ document at persistence boundary
- Application services implement use case interfaces and orchestrate ports

## Module Structure (per module)
```
module/
├── domain/model/           # Pure POJO entities, value objects, enums
├── application/
│   ├── port/in/            # Use case interfaces (input ports)
│   ├── port/out/           # Persistence port interfaces (output ports)
│   └── service/            # Use case implementations
└── infrastructure/
    ├── persistence/mongo/
    │   ├── entity/         # @Document classes
    │   ├── mapper/         # MapStruct or manual mappers
    │   └── repository/     # Spring Data repos + port adapters
    └── web/
        ├── Controller.java # REST controller (uses input ports only)
        └── WebMapper.java  # Domain → DTO mapping
```

## Status
- ✅ All 11 modules refactored to strict hex architecture
- ✅ Clean Maven compilation verified
