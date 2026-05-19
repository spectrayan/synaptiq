# Architecture Decision Records (ADR)

## ADR-001: Adopt Hexagonal Architecture

- **Status:** Accepted
- **Date:** 2025-08-15
- **Context:** Our monolithic service is becoming difficult to test and maintain.
- **Decision:** Adopt hexagonal (ports and adapters) architecture for all new modules. Domain logic must not depend on infrastructure concerns.
- **Consequences:** Higher initial boilerplate, but improved testability and the ability to swap infrastructure components (database, messaging) without touching domain code.

## ADR-002: MongoDB as Primary Data Store

- **Status:** Accepted
- **Date:** 2025-09-01
- **Context:** We need a flexible schema for multi-tenant SaaS with varying data shapes.
- **Decision:** Use MongoDB with Spring Data Reactive for all persistence. Each tenant's data is isolated via a tenantId field on all documents.
- **Consequences:** Strong consistency within a document, eventual consistency for cross-document operations. Must design aggregates carefully.

## ADR-003: Spring AI for LLM Integration

- **Status:** Accepted
- **Date:** 2026-01-10
- **Context:** We need a vendor-agnostic way to integrate LLM providers (Gemini, OpenAI, Ollama) for chat, embeddings, and RAG.
- **Decision:** Use Spring AI with its ChatModel/EmbeddingModel abstractions. Provider switching is achieved through configuration, not code changes.
- **Consequences:** Locked into Spring AI's abstraction layer, but gain portability across LLM providers and built-in vector store integration.

## ADR-004: MongoDB Atlas Vector Search for RAG

- **Status:** Accepted
- **Date:** 2026-02-20
- **Context:** We need vector similarity search for knowledge base retrieval without introducing a separate vector database.
- **Decision:** Use MongoDB Atlas Vector Search via spring-ai-starter-vector-store-mongodb-atlas. Vectors are stored alongside document metadata in the same database.
- **Consequences:** Simpler operations (single database), but limited to MongoDB Atlas for production. Local dev uses a regular MongoDB instance with approximate search.
