# Administration

Manage tenants, users, AI configuration, and branding through Synaptiq's admin interfaces.

---

## User Management

### Roles & Permissions

Synaptiq uses a **scope-based RBAC** model with hierarchical roles:

| Role | Scope | Capabilities |
|------|-------|-------------|
| **Platform Admin** | Global | Manage all tenants, users, and system config |
| **Tenant Admin** | Per-tenant | Manage users, AI config, branding within their tenant |
| **User** | Per-tenant | Full chat, workflow, and knowledge base access |
| **Viewer** | Per-tenant | Read-only access to chat history and workflow results |

### Managing Users

```bash
# List users
GET /api/v1/users

# Create user
POST /api/v1/auth/signup
{
  "email": "user@company.com",
  "password": "secure-password",
  "role": "tenant:user"
}
```

---

## AI Configuration

Configure the AI's behavior, model, and guardrails per tenant:

### Model Selection

| Setting | Description | Default |
|---------|-------------|---------|
| `provider` | LLM provider (gemini, openai, ollama) | `gemini` |
| `model` | Model name | `gemini-2.5-flash` |
| `temperature` | Creativity (0.0–2.0) | `0.7` |
| `maxTokens` | Maximum response length | `8192` |

### Guardrails

Configure behavioral boundaries:

- **Content filters** — block inappropriate or harmful content
- **Domain restrictions** — limit the AI to specific topics
- **Response guidelines** — enforce tone, format, and citation requirements
- **Data handling** — PII/PHI policies for the AI's responses

```bash
PATCH /api/v1/config/ai
{
  "provider": "gemini",
  "model": "gemini-2.5-flash",
  "temperature": 0.7,
  "guardrails": {
    "contentFilter": true,
    "domainRestriction": "healthcare",
    "requireCitations": true
  }
}
```

---

## Branding

Each tenant can customize the visual appearance:

### Theme Configuration

| Setting | Description |
|---------|-------------|
| **Primary Color** | Main brand color (buttons, headers) |
| **Accent Color** | Secondary color (highlights, links) |
| **Logo** | Uploaded logo displayed in the header |
| **Font** | Custom font family |
| **Dark Mode** | Enable/disable dark mode toggle |

### Theme Presets

Tenants can save up to 5 named theme presets:

```bash
PATCH /api/v1/config/branding
{
  "primaryColor": "#00897b",
  "accentColor": "#00bcd4",
  "logoUrl": "/uploads/company-logo.png",
  "fontFamily": "Inter",
  "presetName": "Corporate Teal"
}
```

!!! note "WCAG AA Compliance"
    Synaptiq automatically validates contrast ratios when custom colors are applied. Colors that fail WCAG AA standards are flagged with a warning.

---

## Tenant Management

### Creating Tenants

```bash
POST /api/v1/tenants
{
  "name": "Acme Healthcare",
  "slug": "acme-health",
  "plan": "enterprise",
  "config": {
    "aiProvider": "gemini",
    "maxUsers": 50,
    "features": ["workflows", "knowledge-base", "branding"]
  }
}
```

### Tenant Isolation

Each tenant's data is isolated at the database level:

- All MongoDB documents include a `tenantId` field
- All API requests include an `X-Tenant-ID` header
- RBAC scopes are evaluated per-tenant
- Branding and AI configuration are independent per tenant
