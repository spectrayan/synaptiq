# @synaptiq/utils

> Shared utility functions and helpers used across the Synaptiq frontend.

[![Part of Synaptiq](https://img.shields.io/badge/part%20of-Synaptiq-7C4DFF?style=flat-square)](../../../../README.md)

## Overview

This library contains cross-cutting utility functions, injection tokens, and helper services shared across all Synaptiq frontend libraries and the shell application.

## Contents

| Export | Description |
|--------|-------------|
| `ENVIRONMENT` | Injection token for environment configuration |
| `ApiBaseUrlInterceptor` | HTTP interceptor for API base URL resolution |
| Utility functions | Common helpers for string manipulation, date formatting, etc. |

## Usage

```typescript
import { ENVIRONMENT } from '@synaptiq/utils';
```

## Building

```bash
pnpm nx build utils
```
