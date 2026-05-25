# @synaptiq/auth

> Angular authentication library for the Synaptiq platform.

[![Part of Synaptiq](https://img.shields.io/badge/part%20of-Synaptiq-7C4DFF?style=flat-square)](../../README.md)

## Overview

This library provides authentication services, guards, and UI components for the Synaptiq frontend. It supports both Firebase Auth (multi-tenant with custom claims) and built-in JWT authentication.

## Features

- **`AuthService`** — Manages login, signup, token refresh, and session state
- **`AuthGuard`** — Route guard for protected pages
- **`AuthInterceptor`** — Automatically attaches JWT tokens to outgoing API requests
- **Login Page** — Pre-built login/signup component with Material 3 styling

## Usage

```typescript
import { AuthService } from '@synaptiq/auth';

@Component({ ... })
export class MyComponent {
  private auth = inject(AuthService);

  async login() {
    await this.auth.login('user@example.com', 'password');
  }
}
```

## Building

```bash
pnpm nx build auth
```

## Running Tests

```bash
pnpm nx test auth
```
