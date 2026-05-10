# @synaptiq/theme

> Material 3 theme service with dynamic CSS variable injection for Synaptiq.

[![Part of Synaptiq](https://img.shields.io/badge/part%20of-Synaptiq-7C4DFF?style=flat-square)](../../../README.md)

## Overview

This library manages the dynamic theming system for the Synaptiq frontend. It supports per-tenant branding (colors, fonts, logos) and provides dark/light mode toggling with smooth transitions.

## Features

- **Dynamic Theming** — Applies tenant-specific colors, fonts, and branding at runtime
- **Dark / Light Mode** — System-aware toggle with CSS variable swapping
- **WCAG AA Validation** — Contrast ratio checking for accessibility compliance
- **Theme Presets** — Support for up to 5 named theme presets per tenant
- **CSS Variable Injection** — Injects Material 3 design tokens as CSS custom properties

## Usage

```typescript
import { ThemeService } from '@synaptiq/theme';

@Component({ ... })
export class MyComponent {
  private theme = inject(ThemeService);

  toggleDarkMode() {
    this.theme.toggleTheme();
  }
}
```

## Building

```bash
pnpm nx build theme
```
