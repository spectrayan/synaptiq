# ADR-004: Component DSL Design

**Status:** Accepted  
**Authors:** Spectrayan Team  
**Date:** 2025-07-01

---

## Context

Synaptiq needs the LLM to generate rich, interactive user interfaces. The options are:
1. Generate HTML/CSS directly — security risk, inconsistent styling
2. Generate React/Angular code — complex, unsafe, hard to validate
3. Generate declarative JSON specs — safe, consistent, framework-agnostic

## Decision

Design a **Component DSL** — a declarative JSON format that describes UI components without executable code:

- **20+ component types** covering data visualization, forms, tables, layouts
- **Backend hydration** — LLM emits data references (`ref: "..."`) that the backend fills with real data
- **Schema validation** — each component type has a JSON Schema that validates the spec before rendering
- **Composable layouts** — `composite_view` allows nesting components in grids, tabs, and columns

## Security Properties

| Property | Guarantee |
|----------|-----------|
| No code execution | DSL is pure data structures — no JavaScript, no HTML |
| No data leakage | LLM sees only schema metadata, not actual data values |
| Validated types | Frontend rejects unknown component types |
| Sanitized strings | All text values are escaped before rendering |

## Consequences

- **Positive:** Secure — no attack surface from LLM-generated UI
- **Positive:** Consistent — all components follow Material Design 3
- **Positive:** Extensible — new component types are additive
- **Negative:** Limited to the defined component set (no arbitrary HTML)
- **Negative:** Complex specs for dashboard-grade layouts
