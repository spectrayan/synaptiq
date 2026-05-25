# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.5.0] — 2026-05-25

### Added
- Multi-agent workflow engine with 4 flow types (Sequential, Parallel, Supervisor, Dynamic)
- `agent-flow-spring` library for reusable workflow orchestration
- MongoDB Atlas Vector Store integration for RAG pipeline
- Scope-based RBAC with 46 granular permission scopes
- Dual authentication (Built-in JWT + Firebase Auth)
- Comprehensive MkDocs documentation site

### Changed
- Upgraded to Spring AI 2.0.0-M4 with Google GenAI support
- Switched from Redis to MongoDB for vector storage
- Updated security configuration for workflow API paths

### Fixed
- Resolved dual ChatModel bean conflict (Gemini + Ollama)
- Fixed `isPublic` null field causing workflow deserialization failure
- Fixed `tenant_id` → `tenantId` field name mismatch in MongoDB
- Added graceful RAG degradation when embedding model is unavailable

---

## [0.4.0] — 2026-04-15

### Added
- Dynamic UI Engine with 20+ Component DSL types
- ECharts integration for data visualization
- Per-tenant branding with WCAG AA validation
- Theme presets (up to 5 per tenant)
- Semantic Schema Registry with auto-inference

### Changed
- Migrated to Angular 21 with Signals
- Adopted Angular Material 3 for all UI components

---

## [0.3.0] — 2026-03-01

### Added
- AI Chat Engine with SSE streaming
- Spring AI integration (Gemini, OpenAI adapters)
- Session management with conversation history
- Knowledge Base document ingestion

---

## [0.2.0] — 2026-01-15

### Added
- Multi-tenant architecture with subdomain isolation
- Firebase Auth integration
- OpenAPI contract-first API design
- Angular + Kotlin + Swift SDK generation

---

## [0.1.0] — 2025-12-01

### Added
- Initial project setup with Nx 22 monorepo
- Spring Boot 4.0 backend with WebFlux
- Angular 21 frontend shell
- MongoDB integration with reactive driver
- Docker Compose development environment
