# Contributing

We welcome contributions of all kinds — bug reports, feature requests, documentation improvements, and code contributions.

---

## Getting Started

### 1. Fork & Clone

```bash
git clone https://github.com/<your-username>/synaptiq.git
cd synaptiq
pnpm install
```

### 2. Set Up Development Environment

Follow the [Quick Start Guide](../getting-started/quickstart.md) to get the platform running locally.

### 3. Create a Branch

```bash
git checkout -b feature/my-awesome-feature
```

---

## Development Workflow

### Nx Monorepo

Synaptiq uses **Nx 22** for monorepo management. Always use Nx to run tasks:

```bash
# Build
pnpm nx build synaptiq            # Frontend
pnpm nx build spring-apis      # Backend

# Test
pnpm nx test shell
pnpm nx test spring-apis

# Lint
pnpm nx lint shell

# Serve
pnpm nx serve synaptiq
```

### Backend Development

```bash
# Run backend with dev profile
cd apps/backend/spring-apis
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
mvn test

# Generate API code from OpenAPI spec
pnpm run build:openapi
```

### Frontend Development

```bash
# Serve with hot reload
pnpm nx serve synaptiq

# Build for production
pnpm nx build synaptiq --configuration=production
```

---

## Coding Standards

### Java (Backend)

- **Java 21** features encouraged (records, sealed classes, pattern matching)
- **Hexagonal architecture** — keep domain logic framework-independent
- **Reactive types** — use `Mono<T>` and `Flux<T>` for all service methods
- **Immutable DTOs** — use Java records for API request/response objects

### TypeScript (Frontend)

- **Angular 21** with Signals for state management
- **Standalone components** — no NgModules
- **Strict TypeScript** — `strict: true` in tsconfig
- **Angular Material 3** for UI components

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add workflow template library
fix: resolve session creation race condition
docs: update deployment guide with K8s instructions
refactor: extract RAG pipeline into dedicated service
```

---

## Pull Request Process

1. Ensure your changes pass all tests
2. Update documentation if applicable
3. Submit a PR against the `main` branch
4. Describe your changes clearly in the PR description
5. Link any related issues

---

## Reporting Issues

### Bug Reports

Include:
- Steps to reproduce
- Expected vs. actual behavior
- Environment details (OS, Java version, Node version)
- Relevant logs

### Feature Requests

Include:
- Use case description
- Proposed solution (if any)
- Alternative approaches considered
