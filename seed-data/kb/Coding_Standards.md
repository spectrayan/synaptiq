# Synaptiq Coding Standards — Java & TypeScript

## 1. Java Backend Standards

- Use Java 21 with virtual threads for IO-bound operations
- Follow Google Java Style Guide with 4-space indentation
- All public APIs must have Javadoc with @param and @return tags
- Use Lombok for boilerplate reduction (@Data, @Builder, @RequiredArgsConstructor)
- Prefer immutable objects — use records for DTOs
- Reactive streams (Mono/Flux) for all web layer operations
- Tests: JUnit 5 + Mockito for unit tests, Testcontainers for integration tests

## 2. TypeScript Frontend Standards

- Angular 19 with standalone components (no NgModules)
- Use signals for reactive state management
- Strict TypeScript configuration — no 'any' types except generated SDK code
- CSS: Use CSS custom properties for theming, BEM naming convention
- All components must have unique test IDs for e2e testing

## 3. API Design Standards

- RESTful endpoints following OpenAPI 3.1 specification
- Use X-Tenant-ID header for multi-tenant isolation (not path parameters)
- Consistent error responses using RFC 7807 Problem Details
- Pagination using cursor-based approach for large collections
- All dates in ISO 8601 format (UTC)

## 4. Git Workflow

- Feature branches off 'main', squash-merge PRs
- Conventional commits: feat:, fix:, docs:, refactor:, test:
- All PRs require 1 approval and passing CI checks
- No force pushes to main or release branches
