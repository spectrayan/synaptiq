# Synaptiq OpenAPI — Generated Spring Server Stubs

> Auto-generated Java Spring WebFlux interfaces and DTOs from the Synaptiq OpenAPI 3.0 specification.

[![Part of Synaptiq](https://img.shields.io/badge/part%20of-Synaptiq-7C4DFF?style=flat-square)](../../../../../README.md)

## Overview

This module contains the **generated** Java code produced by the OpenAPI Generator Maven plugin. It provides:

- **API Interfaces** — Spring WebFlux controller interfaces that the backend implements
- **DTOs** — Request/response model classes (POJOs with Jackson annotations)
- **Validation** — Jakarta Bean Validation annotations on model fields

> ⚠️ **Do not edit generated files directly.** Changes will be overwritten on the next `mvn generate-sources` run. Modify the OpenAPI spec in `libs/shared/openapi-spec/` instead.

## Key Classes

| Package | Description |
|---------|-------------|
| `com.synaptiq.infrastructure.in.web.api` | Generated controller interfaces (e.g., `CatalogApi`, `ChatApi`) |
| `com.synaptiq.infrastructure.in.web.dto` | Generated DTOs (e.g., `CatalogItemResponse`, `ProblemDetails`) |

## Regeneration

```bash
# From the monorepo root:
mvn generate-sources -Popenapi-gen -f libs/shared/openapi-spec/pom.xml

# Then reinstall the local artifact:
mvn clean install -f libs/shared/apis/v1/synaptiq-apis/pom.xml
```

## Error Handling

All error responses use the RFC 9457 `ProblemDetails` schema with `application/problem+json` media type. The `ProblemDetails` DTO includes extended fields:

- `code` — Machine-readable error code (e.g., `CATALOG_NOT_FOUND`)
- `timestamp` — ISO 8601 timestamp of the error
- `traceId` — Request trace ID for debugging
- `errors` — Array of field-level validation errors
