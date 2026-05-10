# @synaptiq/dsl-renderer

> The dynamic UI engine — renders AI-generated Component DSL specs as rich, interactive Angular components at runtime.

[![Part of Synaptiq](https://img.shields.io/badge/part%20of-Synaptiq-7C4DFF?style=flat-square)](../../../README.md)

## Overview

The DSL Renderer is the **core of Synaptiq's dynamic UI generation**. When the AI backend generates a Component DSL JSON spec in response to a user's natural language query, this library dynamically renders the appropriate Angular Material 3 component inline within the conversation — no prebuilt screens, no manual UI development.

> Think of it as the **rendering engine** of an AI-native operating system: the LLM decides *what* to show, and the DSL Renderer decides *how* to show it.

## How It Works

```
User: "Show me a sales dashboard for Q1"
  → LLM generates Component DSL JSON (kpi_card, chart, data_table)
  → Backend hydrates item IDs → real data
  → SSE streams hydrated spec to frontend
  → DSL Renderer maps each spec to an Angular component
  → User sees an interactive dashboard with suggestion chips
```

## Component Catalog (20+ types)

| Category | Components | Description |
|----------|------------|-------------|
| **Data Viz** | `kpi_card`, `chart`, `stat_grid`, `metric_table` | KPI cards, ECharts (bar/line/pie/donut), stat grids |
| **Catalog** | `item_card`, `item_grid`, `item_detail`, `comparison_table` | Product/item display, grids, side-by-side comparison |
| **Tables** | `data_table`, `filter_summary`, `result_count` | Sortable/paginated tables, active filter display |
| **Workflow** | `kanban`, `timeline`, `progress_tracker` | Board views, event timelines, step trackers |
| **Forms** | `form_input` | Dynamic forms with validation, conditional visibility, file upload |
| **Actions** | `action_confirm`, `info_banner`, `empty_state` | Confirmation dialogs, info banners, empty states |
| **Layout** | `view` (stack, columns, grid, tabs, sidebar) | Composable dashboard layouts assembled by AI |
| **Navigation** | `launchpad` | Personalized home surface with saved views |

## Design Principles

- **Declarative, not executable** — Component specs are pure JSON data, never code. This eliminates XSS and UI injection attacks by design.
- **Backend hydration** — The LLM outputs item IDs; the backend resolves them to real data. Sensitive fields never reach the LLM.
- **Conversational continuity** — Every component includes `suggestions[]` — chips that guide the user to the next logical action.
- **Semantic, not primitive** — Unlike low-level UI kits (text, row, column), Synaptiq's DSL operates at the business-semantic level (`comparison_table`, `kanban`, `kpi_card`), making it easier for LLMs to generate correctly.

## Usage

```html
<synaptiq-dsl-renderer
  [spec]="componentSpec"
  (action)="onAction($event)"
  (formSubmitted)="onFormSubmit($event)">
</synaptiq-dsl-renderer>
```

## Building

```bash
pnpm nx build dsl-renderer
```

## Running Tests

```bash
pnpm nx test dsl-renderer
```

## Further Reading

- [A2UI Comparison](../../../docs/research/a2ui_vs_synaptiq_analysis.md) — How Synaptiq's DSL compares to Google's A2UI spec
- [Vision](../../../docs/vision.md) — Why dynamic UI generation matters
