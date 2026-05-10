# Camel Integration Engine

Apache Camel-powered **tenant integration engine** with Spring Boot auto-configuration for Synaptiq.

## What it does

Drop this library as a dependency and get:
- **Dynamic route loading** — YAML/JSON route configs stored in DB, loaded/unloaded at runtime
- **Route templates** — parameterized blueprints for REST polling, webhooks, Slack, email, database queries
- **Multi-tenant isolation** — route ID prefixing, tenant header propagation, per-tenant rate limiting
- **300+ Camel connectors** — Salesforce, Slack, databases, message queues, file storage, and more
- **Auto-configuration** — `@AutoConfiguration` wires everything; consumer provides SPI implementations

## Usage

### 1. Add dependency

```xml
<dependency>
    <groupId>com.spectrayan.synaptiq</groupId>
    <artifactId>camel-integration</artifactId>
</dependency>
```

### 2. Implement SPI interfaces

| Interface | Purpose |
|-----------|---------|
| `RouteConfigProvider` | Load/save route configs from your database |
| `CredentialProvider` | Resolve secrets from your secret manager |
| `ExecutionLogger` | Persist execution audit logs |

### 3. Configure

```yaml
synaptiq:
  integration:
    enabled: true
    tenant:
      max-routes-per-tenant: 25
      max-executions-per-day: 5000
```

## Architecture

```
com.spectrayan.synaptiq.integration
├── model/          # RouteConfig, TemplateDescriptor, ExecutionResult (POJOs)
├── spi/            # RouteConfigProvider, CredentialProvider, ExecutionLogger
├── core/           # CamelEngineManager, RouteLifecycleService, RouteAdapterRegistry
├── adapter/        # REST, Webhook, Slack, Database, Email adapters
├── template/       # Built-in route templates + TemplateRegistry
├── tenant/         # TenantIsolationInterceptor, TenantRateLimitPolicy
├── health/         # CamelHealthIndicator, CamelMetricsReporter
└── autoconfigure/  # CamelIntegrationAutoConfiguration + Properties
```
