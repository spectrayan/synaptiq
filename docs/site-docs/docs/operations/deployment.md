# Deployment Guide

Detailed instructions for deploying Synaptiq in development and production environments.

---

## Development Setup

### Docker Compose (Recommended)

```bash
# Start MongoDB Atlas Local with vector search
docker compose up -d

# Verify
docker ps
# Should show: synaptiq-mongodb (port 27017)
```

### Environment Variables

| Variable | Required | Description | Default |
|----------|----------|-------------|---------|
| `GOOGLE_API_KEY` | ✅ | Google Gemini API key | — |
| `AUTH_PROVIDER` | ❌ | `builtin` or `firebase` | `builtin` |
| `MONGODB_URI` | ❌ | MongoDB connection string | `mongodb://localhost:27017/synaptiq` |
| `OLLAMA_BASE_URL` | ❌ | Ollama server URL for embeddings | `http://localhost:11434` |
| `SPRING_PROFILES_ACTIVE` | ❌ | Active Spring profile | `dev` |

### Backend

```bash
GOOGLE_API_KEY="your-key" AUTH_PROVIDER="builtin" \
  mvn spring-boot:run \
    -f apps/backend/spring-apis/pom.xml \
    -Dspring-boot.run.profiles=dev \
    -Dmaven.test.skip=true
```

### Frontend

```bash
pnpm nx serve synaptiq
```

---

## Production Deployment

### Docker Compose

```bash
docker compose -f docker-compose.prod.yml up -d
```

This starts:
- **MongoDB** — persistent storage with volume mount
- **Synaptiq Backend** — Spring Boot JAR
- **Synaptiq Frontend** — Nginx serving Angular build

### Health Checks

| Endpoint | Expected | Purpose |
|----------|----------|---------|
| `GET /actuator/health` | `{"status":"UP"}` | Kubernetes liveness |
| `GET /actuator/health/readiness` | `{"status":"UP"}` | Kubernetes readiness |

### Kubernetes

Adapt the Docker Compose configuration to Kubernetes manifests:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: synaptiq-backend
spec:
  replicas: 2
  template:
    spec:
      containers:
        - name: synaptiq
          image: spectrayan/synaptiq:latest
          ports:
            - containerPort: 8080
          env:
            - name: GOOGLE_API_KEY
              valueFrom:
                secretKeyRef:
                  name: synaptiq-secrets
                  key: google-api-key
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 30
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
```

---

## MongoDB Setup

### Atlas Local (Development)

The Docker Compose file starts MongoDB Atlas Local with vector search support:

```yaml
services:
  mongodb:
    image: mongodb/mongodb-atlas-local:8.0
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
```

### Atlas Cloud (Production)

For production, use MongoDB Atlas Cloud:

1. Create a cluster at [cloud.mongodb.com](https://cloud.mongodb.com)
2. Create a vector search index on the `documents` collection
3. Update `MONGODB_URI` to point to your Atlas cluster

---

## LLM Provider Setup

=== "Google Gemini"
    1. Get an API key from [Google AI Studio](https://aistudio.google.com/apikey)
    2. Set `GOOGLE_API_KEY` environment variable
    3. Model: `gemini-2.5-flash` (recommended)

=== "OpenAI"
    1. Get an API key from [platform.openai.com](https://platform.openai.com)
    2. Set `OPENAI_API_KEY` environment variable
    3. Update `application.yml` to use OpenAI provider

=== "Ollama (Local)"
    1. Install Ollama: `curl -fsSL https://ollama.ai/install.sh | sh`
    2. Pull models: `ollama pull nomic-embed-text`
    3. Start server: `ollama serve`
    4. Used for embeddings by default

---

## Seeding Data

### Automated Seeding

```bash
pip install pymongo && python seed-data/seed_all.py
```

### Manual Seeding

```bash
# Connect to MongoDB
docker exec -it synaptiq-mongodb mongosh synaptiq

# Create admin user
db.users.insertOne({
  email: "admin@synaptiq.dev",
  password: "$2a$10$...",  // bcrypt hash of "admin123"
  roles: ["PLATFORM_ADMIN"],
  tenantId: "demo-tenant"
})
```

---

## Monitoring

### Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health |
| `/actuator/info` | Build information |
| `/actuator/metrics` | Micrometer metrics |
| `/actuator/env` | Environment properties |
