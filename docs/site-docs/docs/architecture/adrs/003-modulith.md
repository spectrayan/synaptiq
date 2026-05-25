# ADR-003: Spring Modulith Boundaries

**Status:** Accepted  
**Authors:** Spectrayan Team  
**Date:** 2025-06-15

---

## Context

Synaptiq needs strong module isolation to enable independent development and future microservice extraction, while keeping the simplicity of a single deployment unit.

## Decision

Use **Spring Modulith** to enforce module boundaries:

- Each module (chat, workflow, knowledgebase, etc.) is a top-level package under `com.spectrayan.synaptiq`
- Modules communicate **only** through domain events via `@ApplicationModuleListener`
- The `shared` module is marked `@ApplicationModule(type = OPEN)` for cross-cutting concerns
- Module boundaries are verified by ArchUnit tests

## Module Map

| Module | Dependencies | Exposed Events |
|--------|-------------|----------------|
| `chat` | `shared`, `auth` | `MessageSent`, `SessionCreated` |
| `workflow` | `shared`, `auth` | `WorkflowExecuted`, `WorkflowCreated` |
| `knowledgebase` | `shared`, `auth` | `DocumentIngested` |
| `schemaregistry` | `shared`, `auth` | `SchemaUpdated` |
| `tenantconfig` | `shared` | `ConfigChanged` |
| `branding` | `shared`, `tenantconfig` | `ThemeUpdated` |

## Consequences

- **Positive:** Clear ownership boundaries — each module can be developed independently
- **Positive:** Event-driven communication prevents tight coupling
- **Positive:** Modules can be extracted to microservices by replacing events with messaging
- **Negative:** Event choreography can be harder to debug than direct calls
