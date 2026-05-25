# Frequently Asked Questions

---

## General

### What is Synaptiq?

Synaptiq is an **AI-native application platform** that generates dynamic user interfaces, dashboards, and workflows from natural language. Instead of building static screens, users describe what they need, and Synaptiq assembles the interface in real-time using a declarative Component DSL.

### Is Synaptiq open source?

Yes. Synaptiq is released under the **MIT License**. You can use, modify, and distribute it freely.

### Can I self-host Synaptiq?

Absolutely. Synaptiq is designed for self-hosting. You need:

- Docker (for MongoDB and infrastructure)
- Java 21+ (for the Spring Boot backend)
- Node.js 22+ (for the Angular frontend)

Your data never leaves your infrastructure.

---

## LLM & AI

### Which LLM providers does Synaptiq support?

| Provider | Status | Notes |
|----------|--------|-------|
| **Google Gemini** | ✅ Supported | Primary provider via Spring AI |
| **OpenAI** | ✅ Supported | BYOK (Bring Your Own Key) |
| **Ollama** | ✅ Supported | Local models, embeddings |
| **Anthropic** | 🔶 Planned | Claude integration |

### Can I use my own API keys?

Yes. Synaptiq supports **BYOK (Bring Your Own Key)** — configure your API key in the environment variables and the platform uses your account directly.

### Does Synaptiq send my data to LLM providers?

The LLM receives:

- The user's natural language query
- The semantic schema (entity names, field types — no actual data)
- System prompts defining the AI's behavior

**The LLM never sees raw business data.** The Component DSL uses data references (`ref: "..."`) that are hydrated server-side after the LLM generates the UI specification.

### What about data privacy with LLM providers?

For maximum privacy:

1. Use **Ollama** for fully local inference
2. Use **Google Gemini** with data processing agreements
3. Configure per-tenant LLM settings for different security levels

---

## Architecture

### What's the Component DSL?

The Component DSL is a JSON specification that describes user interfaces declaratively. Instead of generating HTML or executable code, the LLM emits structured JSON (KPI cards, charts, tables, forms) that the Angular frontend renders natively. This provides security (no code execution) and consistency (all components follow Material Design 3).

→ See [Key Concepts — Component DSL](key-concepts.md#component-dsl) for details.

### Why MongoDB instead of PostgreSQL?

MongoDB was chosen for:

- **Document flexibility** — Component DSL specs, workflow definitions, and semantic schemas are naturally document-shaped
- **Atlas Vector Search** — built-in vector search for RAG without a separate vector database
- **Reactive driver** — excellent support for Spring WebFlux non-blocking I/O
- **Multi-tenant isolation** — tenant-scoped queries with indexed `tenantId` fields

### Why Spring Modulith instead of microservices?

Spring Modulith provides:

- **Module boundaries** without network overhead
- **Event-driven communication** between modules
- **Simpler deployment** — one JAR vs. 10+ services
- **Same DDD patterns** — hexagonal architecture within each module
- **Easy migration** — modules can be extracted to microservices later if needed

---

## Deployment

### What are the minimum system requirements?

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| **CPU** | 2 cores | 4+ cores |
| **RAM** | 4 GB | 8+ GB |
| **Disk** | 10 GB | 50+ GB (for vector store) |
| **Java** | 21 | 21+ |
| **Node.js** | 22 | 22+ |
| **MongoDB** | 7.0 | 8.0+ (Atlas Local for vector search) |

### Can I run Synaptiq in Kubernetes?

Yes. The Docker Compose configuration can be adapted to Kubernetes manifests. The backend is a single Spring Boot JAR with health endpoints (`/actuator/health`) compatible with Kubernetes liveness and readiness probes.

### How do I configure different LLM providers per tenant?

Tenant-level LLM configuration is managed through the Admin API:

```bash
PATCH /api/v1/config/ai
{
  "provider": "gemini",
  "model": "gemini-2.5-flash",
  "apiKey": "your-key",
  "temperature": 0.7,
  "maxTokens": 8192
}
```

---

## Multi-Tenancy

### How does tenant isolation work?

Every document in MongoDB includes a `tenantId` field. All queries are automatically scoped by tenant. Additionally:

- **RBAC** — role-based access control per tenant
- **Branding** — independent themes, logos, and color palettes
- **Configuration** — separate AI persona, guardrails, and model settings

### Can different tenants use different LLM providers?

Yes. Each tenant can be configured with its own LLM provider, model, API key, and parameters.

---

## Contributing

### How can I contribute?

See our [Contributing Guide](../operations/contributing.md). We welcome:

- 🐛 Bug reports
- 💡 Feature requests
- 📝 Documentation improvements
- 🔧 Code contributions

### What's the development workflow?

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `pnpm nx run-many --target=test`
5. Submit a pull request

---

## Support

### Where can I get help?

- 💬 [GitHub Discussions](https://github.com/spectrayan/synaptiq/discussions) — community Q&A
- 🐛 [GitHub Issues](https://github.com/spectrayan/synaptiq/issues) — bug reports and feature requests
- 📧 [developer@spectrayan.com](mailto:developer@spectrayan.com) — direct support
