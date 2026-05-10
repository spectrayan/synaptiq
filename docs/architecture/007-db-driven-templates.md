# ADR-007: DB-Driven Integration Templates

**Status:** Accepted  
**Date:** 2026-05-10  
**Authors:** Spectrayan Team

---

## Context

The initial integration engine hardcoded all templates as Java classes. Adding a new integration required code changes and redeployment — contradicting the n8n-like self-serve goal.

## Decision

Make templates **DB-driven** with built-ins as **seed data**. `connectorType` is a free-form `String` (not an enum). Custom templates carry Camel YAML DSL in a `routeYaml` field.

### Duplicate Protection

| Layer | Where | What |
|-------|-------|------|
| Write guard | `saveCustomTemplate()` | Rejects save if ID matches built-in |
| Read filter | `listTemplates()` | Excludes DB shadows of built-in IDs |
| Lookup priority | `findTemplate()` | Built-in map checked first |

### Adding a New Integration (Zero Code Changes)

1. `POST /api/v1/integrations/templates` — admin creates template with YAML
2. `POST /api/v1/integrations` — tenant creates config using that template
3. `PUT /api/v1/integrations/{id}/activate` — engine loads into CamelContext

## Consequences

### Positive
- Zero code changes for new integration types
- Tenants can self-serve custom integrations
- Built-in templates serve as reference implementations

### Negative
- Custom YAML templates are harder to validate than compiled Java
- Two template sources add query complexity (mitigated by composite registry)
