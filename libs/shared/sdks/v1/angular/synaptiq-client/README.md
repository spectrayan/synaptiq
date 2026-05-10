# @synaptiq/client — Angular SDK

> Auto-generated Angular HTTP client for the Synaptiq REST API.

[![Part of Synaptiq](https://img.shields.io/badge/part%20of-Synaptiq-7C4DFF?style=flat-square)](../../../../../../README.md)

## Overview

This package is the **generated** Angular TypeScript SDK produced by the OpenAPI Generator. It provides type-safe, injectable Angular services for every Synaptiq API endpoint.

## Services

| Service | Description |
|---------|-------------|
| `CatalogService` | Catalog schema import, item CRUD, CSV upload |
| `ChatService` | SSE streaming chat, session management |
| `TenantsService` | Tenant CRUD, admin management |
| `ConfigService` | AI persona, guardrails, LLM provider config |
| `BrandingService` | Theme, colors, logo, personalization |
| `AnalyticsService` | Usage metrics, billing, platform analytics |
| `WorkflowsService` | Workflow CRUD, execution, templates |
| `ActionsService` | Action execution (save items, enquiries) |
| `AuthService` | Login, signup, token refresh |

## Installation

This package is consumed as a workspace dependency — no npm install needed:

```typescript
// In your Angular module or standalone component:
import { CatalogService, ChatService } from '@synaptiq/client';
```

## Configuration

```typescript
import { ApiModule, Configuration } from '@synaptiq/client';

@NgModule({
  imports: [
    ApiModule.forRoot(() => new Configuration({
      basePath: 'http://localhost:8080'
    }))
  ]
})
export class AppModule {}
```

## Regeneration

```bash
# From the monorepo root:
mvn generate-sources -Popenapi-gen -f libs/shared/openapi-spec/pom.xml

# Build the SDK:
pnpm run build --filter @synaptiq/client
```

> ⚠️ **Do not edit generated files directly.** Modify the OpenAPI spec in `libs/shared/openapi-spec/` instead.
