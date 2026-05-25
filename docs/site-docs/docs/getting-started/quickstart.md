# Quick Start

Get Synaptiq running locally in under 10 minutes.

---

## Prerequisites

| Tool | Version | Check |
|------|---------|-------|
| **Java** | 21+ (JDK) | `java --version` |
| **Node.js** | 22+ | `node --version` |
| **pnpm** | 10+ | `pnpm --version` |
| **Maven** | 3.9+ | `mvn --version` |
| **Docker** | Latest | `docker --version` |

---

## 1. Clone & Install

```bash
git clone https://github.com/spectrayan/synaptiq.git
cd synaptiq
pnpm install
```

---

## 2. Start Infrastructure

```bash
# Start MongoDB Atlas Local (with vector search support)
docker compose up -d
```

This starts:

- **MongoDB 8** — Atlas Local with vector search capabilities
- Network configuration for replica set support

!!! tip "Verify infrastructure"
    ```bash
    docker ps
    # Should show mongo-vector container running
    ```

---

## 3. Configure Environment

Create or update your `.env` file with your LLM API key:

```bash
# Required: Google Gemini API key for chat
GOOGLE_API_KEY=your-gemini-api-key

# Auth mode: 'builtin' for local JWT, 'firebase' for Firebase Auth
AUTH_PROVIDER=builtin

# Optional: Ollama for local embeddings (required for RAG/vector search)
# Ensure Ollama is running: ollama serve
# Pull the embedding model: ollama pull nomic-embed-text
```

!!! note "Getting a Gemini API Key"
    1. Go to [Google AI Studio](https://aistudio.google.com/apikey)
    2. Create a new API key
    3. Copy it to your `.env` file

---

## 4. Seed Demo Data (Optional)

```bash
# Seed users, tenants, and sample workflows
pip install pymongo && python seed-data/seed_all.py
```

This creates:

| Data | Details |
|------|---------|
| **Admin User** | `admin@synaptiq.dev` / `admin123` |
| **Demo Tenant** | `demo-tenant` with sample configuration |
| **Sample Workflows** | ABA Therapy Goal Generator and templates |
| **RBAC Scopes** | 46 permission scopes with role mappings |

---

## 5. Start the Platform

=== "Both Services"

    ```bash
    # Terminal 1: Backend (Spring Boot on :8080)
    GOOGLE_API_KEY="your-key" AUTH_PROVIDER="builtin" \
      mvn spring-boot:run \
        -f apps/backend/spring-apis/pom.xml \
        -Dspring-boot.run.profiles=dev

    # Terminal 2: Frontend (Angular on :4200)
    pnpm nx serve synaptiq
    ```

=== "Backend Only"

    ```bash
    cd apps/backend/spring-apis
    GOOGLE_API_KEY="your-key" AUTH_PROVIDER="builtin" \
      mvn spring-boot:run -Dspring-boot.run.profiles=dev
    ```

=== "Frontend Only"

    ```bash
    pnpm nx serve synaptiq
    ```

---

## 6. Access the Application

| Service | URL |
|---------|-----|
| **Frontend** | [http://localhost:4200](http://localhost:4200) |
| **Backend API** | [http://localhost:8080](http://localhost:8080) |
| **Swagger UI** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| **Health Check** | [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) |

### Default Login

| Field | Value |
|-------|-------|
| **Email** | `admin@synaptiq.dev` |
| **Password** | `admin123` |

---

## 7. First Steps After Login

1. **Chat** — Send a message like *"Hello, what can you do?"* to verify the LLM connection
2. **Workflows** — Navigate to the Workflow tab to see sample workflows
3. **Create a Workflow** — Try *"Generate a workflow for customer onboarding"*
4. **Execute** — Run a workflow and observe the multi-agent execution

---

## Troubleshooting

??? question "Backend fails to start with bean conflict"
    If you see `required a single bean, but 2 were found` for `ChatModel`:

    This means both Gemini and Ollama chat models are active. The `dev` profile should exclude `OllamaChatAutoConfiguration`. Check `application-dev.yml`:

    ```yaml
    spring:
      autoconfigure:
        exclude:
          - org.springframework.ai.model.ollama.autoconfigure.OllamaChatAutoConfiguration
    ```

??? question "Frontend shows 'Network Error'"
    Ensure the backend is running on port 8080. Check:

    ```bash
    curl http://localhost:8080/actuator/health
    # Should return: {"status":"UP"}
    ```

??? question "Chat returns empty responses"
    Verify your `GOOGLE_API_KEY` is valid:

    ```bash
    curl "https://generativelanguage.googleapis.com/v1beta/models?key=YOUR_KEY"
    ```

??? question "Workflows don't appear in the UI"
    Ensure you've seeded the database and the workflow documents have the correct `tenantId` field (camelCase, not `tenant_id`).

---

## Next Steps

- 📖 [Platform Overview](platform-overview.md) — Tour the key features
- 💬 [Chat & AI Guide](../user-guide/chat.md) — Learn the chat interface
- 🔧 [Workflow Designer](../user-guide/workflows.md) — Build multi-agent workflows
- 🏗️ [Architecture](../architecture/overview.md) — Understand the system design
