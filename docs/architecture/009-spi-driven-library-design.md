# ADR-009: SPI-Driven Library Design

**Status:** Accepted  
**Date:** 2026-05-10  
**Authors:** Spectrayan Team

---

## Context

The `camel-integration` library needs to be reusable across different applications. It requires persistence (route configs, templates), credential resolution, and execution logging — but should not dictate which database, secret manager, or logging backend the consuming application uses.

## Decision

Design the library around **Service Provider Interfaces (SPIs)** with Spring Boot `@AutoConfiguration`. The library declares interfaces; the consuming application provides implementations.

### SPI Contracts

| SPI | Purpose | Implementation in spring-apis |
|-----|---------|-------------------------------|
| `RouteConfigProvider` | CRUD for route configurations | `MongoRouteConfigProvider` |
| `TemplateConfigProvider` | CRUD for custom templates | `MongoTemplateConfigProvider` |
| `CredentialProvider` | Resolve encrypted secrets | `MongoCredentialProvider` (stub) |
| `ExecutionLogger` | Audit log for route executions | `MongoExecutionLogger` |

### Auto-Configuration Strategy

```java
@AutoConfiguration
@ConditionalOnClass(CamelContext.class)
@ConditionalOnProperty(prefix = "synaptiq.integration", name = "enabled", matchIfMissing = true)
public class CamelIntegrationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({RouteConfigProvider.class, CredentialProvider.class})
    public RouteLifecycleService routeLifecycleService(...) { }

    @Bean
    @ConditionalOnMissingBean
    public TemplateRegistry templateRegistry(Optional<TemplateConfigProvider> provider) { }
}
```

### Rules

1. **Library never imports Spring Data, MongoDB, or any persistence framework**
2. **All persistence goes through SPI interfaces** — `Mono<T>` / `Flux<T>` return types
3. **`@ConditionalOnBean`** ensures services only activate when SPIs are provided
4. **`Optional<T>`** for non-critical SPIs (e.g., `TemplateConfigProvider` — works without DB)
5. **`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`** registers the auto-config

## Consequences

### Positive
- Library is truly reusable — swap MongoDB for PostgreSQL by implementing the SPIs
- Zero-config adoption: add dependency → provide SPI beans → integration engine activates
- Clean separation: library owns Camel logic, application owns persistence

### Negative
- SPI interfaces must be stable — breaking changes affect all consumers
- More interfaces to implement when adopting the library (mitigated by clear contracts)
