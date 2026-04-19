---
description: Run integration tests for the health-app module (requires Docker for Testcontainers)
---

# Run Integration Tests

This workflow runs the health-app integration test suite using Testcontainers with MongoDB.

## Prerequisites
- Docker must be running (required for Testcontainers MongoDB container)
- Java 21+ and Maven 3.9+

## Steps

### 1. Close VS Code Java Language Server (Important!)
The VS Code Java extension (Eclipse JDT) can corrupt MapStruct-generated classes in `target/classes`.
Before running tests, disable the Java language server or close the project.

Alternatively, you can ignore this step - the workflow below handles the issue by using
`install` first and then running `test` without `-am`.

// turbo
### 2. Clean install all dependencies (skip tests)
```bash
mvn clean install -DskipTests -pl apps/backend/core/health-app -am
```

// turbo
### 3. Run integration tests (from installed JARs)
```bash
mvn test -pl apps/backend/core/health-app
```

Running WITHOUT `-am` ensures surefire uses the installed JARs from `.m2` 
instead of `target/classes` directories that may be corrupted by VS Code JDT.

## Expected Results
- 12 test classes across 4 modules: Provider, Client, Questionnaire, User
- ~37 total tests (2 skipped due to seed data schema mismatch in ParentApi)
- All non-skipped tests should PASS

## Troubleshooting
If you see `NoSuchBeanDefinitionException` for `ProviderDomainEntityMapper`:
1. This means VS Code JDT overwrote the MapStruct generated class
2. Run `mvn clean install -DskipTests -pl apps/backend/core/provider/provider-app -am`
3. Then re-run the tests: `mvn test -pl apps/backend/core/health-app`
