# @synaptiq/constants

> Shared TypeScript constants, types, and enums used across the Synaptiq frontend.

[![Part of Synaptiq](https://img.shields.io/badge/part%20of-Synaptiq-7C4DFF?style=flat-square)](../../../../README.md)

## Overview

This library provides the shared type definitions and constants that are consumed by both the Angular shell application and the frontend feature libraries. It serves as the single source of truth for DSL component types, view specifications, and shared enums.

## Contents

| Export | Description |
|--------|-------------|
| `ComponentSpec` | Type definition for all 10 DSL component specifications |
| `ViewSpec` | Type for pinned view configurations |
| `ComponentType` | Union type of all supported DSL component type keys |
| Enums | Shared enumerations for roles, status values, etc. |

## Usage

```typescript
import { ComponentSpec, ViewSpec } from '@synaptiq/constants';
```

## Building

```bash
pnpm nx build constants
```
