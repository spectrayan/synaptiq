# Contributing to Synaptiq

Thank you for your interest in contributing to Synaptiq! This document provides guidelines and instructions for contributing.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Making Changes](#making-changes)
- [Coding Standards](#coding-standards)
- [Pull Request Process](#pull-request-process)
- [Reporting Issues](#reporting-issues)

## Code of Conduct

This project adheres to the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to [support@spectrayan.com](mailto:support@spectrayan.com).

## Getting Started

1. **Fork** the repository on GitHub
2. **Clone** your fork locally
3. **Create a branch** for your change
4. **Make your changes** with appropriate tests
5. **Submit a pull request**

## Development Setup

### Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ (JDK) |
| Node.js | 22+ |
| pnpm | 10+ |
| Maven | 3.9+ |
| Docker | Latest |
| Python | 3.12+ (optional, for seed scripts only) |

### First-Time Setup

```bash
# Clone your fork
git clone https://github.com/<your-username>/synaptiq.git
cd synaptiq

# Install frontend dependencies
pnpm install

# Start infrastructure (MongoDB + Redis + Firebase Auth Emulator)
docker compose up -d mongodb redis firebase-auth

# Seed demo data (optional — requires Python + pymongo)
pip install pymongo
python seed-data/seed_all.py

# Start the platform
# Terminal 1: Backend
cd apps/backend/spring-apis && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 2: Frontend
pnpm nx serve shell
```

### Running Individually

```bash
# Backend only (Spring Boot on :8080)
cd apps/backend/spring-apis && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend only (Angular on :4200)
pnpm nx serve shell

# Using helper scripts
./scripts/start-dev.sh    # macOS/Linux
scripts\start-dev.bat     # Windows
```

### Running Tests

```bash
# Backend unit + integration tests
cd apps/backend/spring-apis && mvn test

# Frontend unit tests
pnpm nx test shell

# All affected tests
pnpm nx affected -t test
```

## Making Changes

### Branch Naming

Use descriptive branch names with a type prefix:

```
feat/add-notification-preferences
fix/sse-reconnect-race-condition
refactor/config-properties-hierarchy
docs/contributing-guide
```

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(backend): add notification count endpoint
fix(frontend): gate SSE connection behind auth state
refactor(catalog): consolidate schema validation logic
docs: add contributing guide
chore: update gitignore
```

**Format:** `<type>(<scope>): <description>`

| Type | Purpose |
|------|---------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code restructuring (no behavior change) |
| `docs` | Documentation only |
| `test` | Adding or updating tests |
| `chore` | Build, CI, tooling changes |
| `perf` | Performance improvement |

## Coding Standards

### Backend (Java / Spring)

- **Java 21** — use records, sealed classes, pattern matching where appropriate
- **Spring Modulith** — respect module boundaries; modules communicate via events only
- **Hexagonal architecture** — domain core must be framework-free POJOs
- **Reactive** — use `Mono`/`Flux` throughout; no blocking calls
- **API-first** — changes to REST endpoints start in the OpenAPI spec, then regenerate
- **Testing** — new features require unit tests; use `@WebFluxTest` for controllers

### Frontend (Angular / TypeScript)

- **Angular 21** — standalone components, signals, new control flow (`@if`, `@for`, `@switch`)
- **RxJS + Signals** — signals for reactive UI state; RxJS `EventSource` for SSE streaming
- **Material 3** — use Angular Material components with M3 theming
- **TypeScript strict mode** — no `any` types, proper null checks
- **Generated SDK** — use `@synaptiq/client` for API calls, never raw `HttpClient`

### API Changes

If your change modifies a REST API:

1. Update the OpenAPI spec in `libs/shared/openapi-spec/`
2. Regenerate server interfaces and client SDKs
3. Implement the generated interface in your controller
4. Update any affected SDK consumers

## Pull Request Process

1. **Ensure your branch is up to date** with `main`
2. **All tests pass** — CI will verify this automatically
3. **Fill out the PR template** — describe what changed and why
4. **Link related issues** — use `Closes #123` or `Fixes #456`
5. **One approval required** — a maintainer will review your PR
6. **Squash merge** — PRs are squash-merged to keep history clean

### PR Checklist

- [ ] Code follows the project's coding standards
- [ ] Tests added/updated for the change
- [ ] Documentation updated if needed
- [ ] OpenAPI spec updated if API changed
- [ ] No hardcoded secrets or credentials
- [ ] Commit messages follow Conventional Commits

## Reporting Issues

### Bug Reports

Use the [Bug Report template](https://github.com/spectrayan/synaptiq/issues/new?template=bug_report.md) and include:

- Steps to reproduce
- Expected vs actual behavior
- Environment details (OS, Java version, browser)
- Relevant logs or screenshots

### Feature Requests

Use the [Feature Request template](https://github.com/spectrayan/synaptiq/issues/new?template=feature_request.md) and describe:

- The problem you're trying to solve
- Your proposed solution
- Any alternatives you've considered

## Questions?

- **General questions:** Open a [Discussion](https://github.com/spectrayan/synaptiq/discussions)
- **Bug reports:** Open an [Issue](https://github.com/spectrayan/synaptiq/issues)
- **Security vulnerabilities:** See [SECURITY.md](SECURITY.md)
- **Email:** [developer@spectrayan.com](mailto:developer@spectrayan.com)

---

Thank you for contributing to Synaptiq! 🚀
