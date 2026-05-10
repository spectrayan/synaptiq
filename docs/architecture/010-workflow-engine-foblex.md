# ADR-010: Visual Workflow Engine with Foblex Flow

**Status:** Accepted  
**Date:** 2026-05-09  
**Authors:** Spectrayan Team

---

## Context

Synaptiq needs a visual workflow builder for defining multi-step agent flows. Users should be able to drag-and-drop nodes on a canvas, connect them with edges, and configure node parameters — similar to n8n or Langflow.

We evaluated:
1. **ReactFlow** — React-based, would require Angular wrapper
2. **Foblex Flow** — Angular-native, canvas-based, good DX
3. **Custom canvas with HTML5 Canvas/SVG** — full control but months of development

## Decision

Use **Foblex Flow** (`@foblex/flow`) as the Angular-native canvas library for the visual workflow editor.

### Workflow Architecture

| Layer | Responsibility |
|-------|----------------|
| **Canvas (Foblex)** | Node rendering, edge connections, drag-and-drop, zoom/pan |
| **WorkflowFacade** | State management (signals), API calls, auto-save |
| **Inspector Panel** | Node configuration sidebar (dynamic form based on node type) |
| **Backend** | `agent-flow-spring` library — workflow persistence and execution |

### Workflow Data Model

```typescript
interface WorkflowSpec {
  id: string;
  name: string;
  nodes: WorkflowNode[];
  edges: WorkflowEdge[];
}

interface WorkflowNode {
  id: string;
  type: 'trigger' | 'action' | 'condition' | 'llm' | 'output';
  position: { x: number; y: number };
  config: Record<string, any>;
}
```

### Rules

1. **Workflow specs are persisted as JSON** in MongoDB
2. **Auto-save on every canvas change** — debounced 1s
3. **Flexbox layout** for canvas + inspector side-by-side (no overlap)
4. **Backend execution** via `agent-flow-spring` library (separate from Camel integration routes)

## Consequences

### Positive
- Angular-native — no React bridge needed
- Rich canvas interactions out of the box (zoom, pan, snap-to-grid)
- Workflow specs are portable JSON — can be exported/imported

### Negative
- Foblex Flow is less mature than ReactFlow (smaller community)
- Limited built-in node types — must build custom node components
