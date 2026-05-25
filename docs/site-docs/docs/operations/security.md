# Security

## Reporting Vulnerabilities

If you discover a security vulnerability in Synaptiq, please report it responsibly:

📧 **Email:** [security@spectrayan.com](mailto:security@spectrayan.com)

Please include:
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)

We aim to acknowledge reports within 48 hours and provide a fix within 7 business days for critical issues.

---

## Security Architecture

### Authentication

| Feature | Implementation |
|---------|---------------|
| **Password hashing** | bcrypt with 10 rounds |
| **JWT tokens** | HS256 signed, 24-hour expiry |
| **Refresh tokens** | Secure, HTTP-only cookies |
| **Multi-factor auth** | Via Firebase Auth (production) |

### Authorization

| Feature | Implementation |
|---------|---------------|
| **RBAC** | Scope-based authorization manager |
| **Tenant isolation** | All queries filtered by `tenantId` |
| **API scopes** | 46 granular permission scopes |
| **Path-based auth** | Database-stored path → scope mappings |

### Data Security

| Feature | Implementation |
|---------|---------------|
| **LLM data isolation** | Backend hydration — LLM never sees raw data |
| **Component DSL** | No executable code in UI specs |
| **Input sanitization** | All user input sanitized before storage |
| **CORS** | Strict origin allowlist |

### Infrastructure

| Feature | Implementation |
|---------|---------------|
| **HTTPS** | TLS 1.3 in production |
| **Database** | MongoDB with authentication enabled |
| **Secrets** | Environment variables, no hardcoded keys |
| **Dependencies** | Regular dependency audits via `npm audit` and Maven |

---

## Supported Versions

| Version | Supported |
|---------|-----------|
| Latest | ✅ |
| Previous minor | ✅ |
| Older | ❌ |
