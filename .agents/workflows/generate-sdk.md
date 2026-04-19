---
description: Regenerate Angular, Kotlin, and Swift SDKs after OpenAPI spec changes
---

1. Generate all code from OpenAPI specs (Angular + Kotlin + Swift SDKs):
```bash
mvn generate-sources -Pall-profiles -f apps/backend/core/common/pom.xml
```

2. Or generate individual SDKs:
```bash
# Angular SDK only
mvn generate-sources -Pwellness-angular-sdk -f apps/backend/core/common/pom.xml

# Kotlin Multiplatform SDK only (for Android/iOS)
mvn generate-sources -Pwellness-kotlin-sdk -f apps/backend/core/common/pom.xml

# Swift SDK only (for native iOS)
mvn generate-sources -Pwellness-swift-sdk -f apps/backend/core/common/pom.xml
```

3. Build the Angular SDK library:
```bash
nx build angular-sdk
```

4. Rebuild affected frontend apps:
```bash
nx run-many --target=build --projects=provider,client,tenant
```
