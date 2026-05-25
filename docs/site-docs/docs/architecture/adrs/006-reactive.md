# ADR-006: Reactive Persistence

**Status:** Accepted  
**Authors:** Spectrayan Team  
**Date:** 2025-06-20

---

## Context

Synaptiq uses SSE streaming for chat responses and needs non-blocking I/O throughout the stack to avoid thread exhaustion under load.

## Decision

Adopt **reactive persistence** with Spring WebFlux and MongoDB Reactive Driver:

- All API endpoints return `Mono<T>` or `Flux<T>`
- Repository interfaces extend `ReactiveMongoRepository`
- MongoDB driver uses the reactive streams protocol
- SSE endpoints use `Flux<ServerSentEvent<T>>` for streaming

```java
public interface WorkflowRepository 
    extends ReactiveMongoRepository<WorkflowDocument, String> {
    
    Flux<WorkflowDocument> findByTenantId(String tenantId);
    Mono<WorkflowDocument> findByIdAndTenantId(String id, String tenantId);
}
```

## Consequences

- **Positive:** Non-blocking end-to-end — scales to thousands of concurrent SSE streams
- **Positive:** Back-pressure support — MongoDB driver respects downstream demand
- **Positive:** Natural fit for SSE streaming responses
- **Negative:** Reactive programming has a steeper learning curve
- **Negative:** Debugging stack traces is harder with reactive chains
- **Negative:** Some Spring features (e.g., `@Transactional`) work differently in reactive context
