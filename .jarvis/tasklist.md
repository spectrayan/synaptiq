# Synaptiq Java Backend — Task List

## Completed ✅
- [x] Create `apps/backend/spring-apis` Maven project with Spring Boot 4
- [x] Configure Nx project.json (build/serve/test targets)
- [x] Implement shared module (SynaptiqProperties, SecurityConfig, CorsConfig, JacksonConfig)
- [x] Implement TenantResolutionFilter (WebFilter matching Python TenantMiddleware)
- [x] Implement GlobalExceptionHandler with structured error responses
- [x] Implement Tenant module (full hexagonal: domain → persistence → service → controller)
- [x] Implement Catalog module (CatalogSchema, CatalogItem, service, controller)
- [x] Implement Chat module (Session, SSE streaming, session CRUD)
- [x] Implement Auth module (signup, login, JWT, /me endpoint)
- [x] Implement Workflow module (CRUD, SSE generate/execute, share, templates)
- [x] Implement Analytics module (summary, tokens, billing, platform rollup)
- [x] Implement Actions module (dispatch, saved items, audit log)
- [x] Implement Branding module (branding, themes, contrast check, personalization)
- [x] Implement TenantConfig module (AI persona, guardrails, LLM provider, components)
- [x] Implement SchemaRegistry module (collections, infer, query)
- [x] Add Spring Modulith package-info.java for all 11 modules
- [x] Verify clean Maven compilation

- [x] Wire Chat SSE to Spring AI ChatClient (Gemini/Vertex)
- [x] Wire Workflow executor to agent-flow-spring library

## In Progress 🔄

## Backlog 📋
- [x] MongoDB aggregation pipelines for Analytics (replace stubs)
- [x] CSV import endpoint for Catalog
- [x] Rate limiting WebFilter (Redis-based)
- [x] Integration tests with Testcontainers
- [x] Spring Modulith event-based inter-module communication
- [x] OpenAPI spec generation via SpringDoc
- [x] Docker Compose service entry for spring-apis
