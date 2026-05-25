# ADR-001: Hexagonal Architecture

**Status:** Accepted  
**Authors:** Spectrayan Team  
**Date:** 2025-06-15

---

## Context

Synaptiq requires a clean separation between business logic and infrastructure concerns. The domain model (workflows, chat sessions, tenant configuration) must be testable independently of Spring Boot, MongoDB, or LLM providers.

## Decision

Adopt **hexagonal architecture** (ports & adapters) for all Spring Modulith modules:

```
module/
├── domain/model/          # Pure POJOs — entities, value objects, domain events
├── application/
│   ├── service/           # Use case implementations
│   └── port/
│       ├── in/            # Driving ports (interfaces called by controllers)
│       └── out/           # Driven ports (interfaces implemented by adapters)
└── infrastructure/
    ├── web/               # REST controllers (driving adapters)
    └── persistence/       # MongoDB repositories (driven adapters)
```

## Consequences

- **Positive:** Domain logic is framework-independent and fully unit-testable
- **Positive:** Infrastructure can be swapped (e.g., MongoDB → PostgreSQL) without touching domain
- **Positive:** Clear dependency direction: infrastructure → application → domain
- **Negative:** More boilerplate (port interfaces, mapper classes)
- **Negative:** Learning curve for new contributors
