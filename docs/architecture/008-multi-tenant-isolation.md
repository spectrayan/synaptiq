# ADR-008: Multi-Tenant Isolation Strategy

**Status:** Accepted  
**Date:** 2026-05-05  
**Authors:** Spectrayan Team

---

## Context

Synaptiq is a multi-tenant SaaS platform where tenants share the same application instance and MongoDB cluster. Data isolation is critical — one tenant must never access another's data, integrations, or configurations.

## Decision

Implement **row-level tenant isolation** with defense-in-depth at every layer.

### Isolation Layers

| Layer | Mechanism |
|-------|-----------|
| **DNS/Routing** | Wildcard `*.spectrayan.com` → tenant slug extraction from Host header |
| **API** | `X-Tenant-Id` header injected by middleware, validated on every request |
| **Database** | `tenantId` field on every document + compound indexes `{tenantId, status}` |
| **Camel Routes** | Route naming `{tenantId}__{routeConfigId}` + `TenantIsolationInterceptor` |
| **Rate Limiting** | `TenantRateLimitPolicy` — per-tenant max route count |
| **Vector Search** | Tenant-scoped `$match` filter on all Atlas Vector Search queries |
| **Cache** | All Redis keys namespaced: `tenant:{id}:*` |

### Route ID Convention

```
Format: {tenantId}__{routeConfigId}
Example: tenant-abc__route-123

Enables:
- Per-tenant route listing via prefix scan
- Tenant context extraction from any Camel exchange
- Collision-free IDs across tenants
```

## Consequences

### Positive
- Single database, single application — minimal operational overhead
- Tenant isolation enforced at every layer — defense in depth
- No per-tenant infrastructure provisioning needed

### Negative
- Noisy neighbor risk on shared compute (mitigated by rate limiting)
- Single database means all tenants share connection pool
- Row-level filtering must be consistently applied (enforced by repository layer)
