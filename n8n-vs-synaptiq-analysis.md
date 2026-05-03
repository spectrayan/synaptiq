# n8n vs Synaptiq — Competitive Architecture Analysis

## Executive Summary

**n8n** is a mature, general-purpose **workflow automation platform** (think Zapier-with-code) that has bolted on AI capabilities. **Synaptiq** is an **AI-native agentic workflow engine** where workflows are first-class AI constructs — generated from natural language, executed by autonomous LLM agents, and (uniquely) **self-modifiable at runtime** by a workflow manager agent.

They solve adjacent but fundamentally different problems.

---

## 1. Design Philosophy

| Aspect | n8n | Synaptiq |
|---|---|---|
| **Core Identity** | Integration automation (iPaaS) | AI agent orchestration |
| **Primary User** | Ops / DevOps / No-code builders | AI engineers / Data scientists |
| **Workflow Origin** | Manual drag-and-drop in visual editor | **AI-generated from natural language** |
| **Node = ?** | A pre-built connector (HTTP, Slack, DB, etc.) | An **autonomous LLM agent** with its own system prompt |
| **AI Role** | AI is one node type among 400+ | AI agents ARE the workflow primitives |
| **Data Model** | Items (JSON arrays) flowing through pipes | Accumulated context passed between agent conversations |

> [!IMPORTANT]
> **The fundamental difference**: In n8n, a workflow is a data pipeline that occasionally calls an AI. In Synaptiq, a workflow IS a coordinated team of AI agents where the agents themselves decide what to do.

---

## 2. Architecture Comparison

### n8n Architecture
```
┌─ packages/ ─────────────────────────────────────┐
│  @n8n/nodes-base    — 400+ integration nodes    │
│  @n8n/workflow       — Core graph + interfaces   │
│  @n8n/core          — WorkflowExecute engine     │
│  @n8n/cli            — Express REST API          │
│  @n8n/editor-ui      — Vue.js visual canvas      │
│  @n8n/design-system  — Shared UI components      │
│  @n8n/benchmark      — Performance testing       │
└─────────────────────────────────────────────────┘
Tech: TypeScript monorepo (pnpm), PostgreSQL/SQLite, Vue 3, Express
Size: ~900K+ LoC across 30+ packages
```

### Synaptiq Architecture
```
┌─ apps/ ─────────────────────────────────────────┐
│  backend/api         — FastAPI + LangChain       │
│    ├─ workflow_service.py  (NL → spec generator) │
│    ├─ workflow_executor.py (SSE step executor)   │
│    └─ llm_provider.py (multi-provider adapter)   │
│  frontend/web/shell — Angular + Signal-based UI  │
│                                                  │
├─ libs/ ──────────────────────────────────────────┤
│  frontend/chat      — WorkflowCanvasComponent    │
│                      — WorkflowService           │
└─────────────────────────────────────────────────┘
Tech: Python/FastAPI backend, Angular 21 frontend, MongoDB, SSE streaming
Size: Lean (~5K LoC for workflow system)
```

---

## 3. Workflow Graph Model

### n8n: `INode[]` + `IConnection[]`
```typescript
// n8n node — a typed integration connector
interface INode {
  name: string;
  type: string;              // e.g. "n8n-nodes-base.httpRequest"
  typeVersion: number;
  position: [number, number];
  parameters: INodeParameters;  // Static config: URLs, auth, expressions
  credentials?: INodeCredentials;
}

// n8n connection — output index → input index
interface IConnection {
  node: string;
  type: NodeConnectionType;   // 'main' | 'ai_tool' | 'ai_memory' etc.
  index: number;
}
```

### Synaptiq: `AgentNodeSpec[]` + `EdgeSpec[]`
```typescript
// Synaptiq node — an autonomous LLM agent
interface AgentNodeSpec {
  id: string;
  type: string;           // "agent" | "tool" | "function" | "conditional" | "parallel"
  label: string;
  description: string;
  system_prompt: string;  // ← Each node has its OWN LLM personality
  llm: { provider: string; model: string };  // Per-node model selection
  tools: string[];        // Agent can use tools autonomously
}

// Synaptiq edge — with conditional routing
interface EdgeSpec {
  from: string;
  to: string;
  condition: string;     // "always" | custom
  label: string;
}

// Plus: conditional_edges with condition_mapping for dynamic routing
```

> [!TIP]
> **Key Difference**: n8n nodes are *configured connectors* — they receive data and transform it deterministically. Synaptiq nodes are *autonomous agents* — each has its own system prompt, can use tools, and produces LLM-generated output that accumulates as context for downstream agents.

---

## 4. Execution Engine

### n8n: `WorkflowExecute.processRunExecutionData()`
- **Stack-based execution**: Uses a `nodeExecutionStack` — pops nodes, runs them, pushes successors
- **Multi-input waiting**: Nodes with multiple inputs wait until ALL inputs arrive (fan-in)
- **Partial execution**: Can re-run a subgraph from a dirty node (complex cycle detection via `handleCycles()`)
- **Cancellable**: Wraps in `PCancelable` for mid-execution abort
- **Data-centric**: Each node produces `INodeExecutionData[][]` — items with JSON + binary data
- **~2,700 lines** of execution logic in `workflow-execute.ts` alone

```typescript
// n8n execution loop (simplified)
while (nodeExecutionStack.length > 0) {
  const executionData = nodeExecutionStack.shift();
  const nodeOutput = await this.executeNode(executionData);
  // Push successor nodes onto stack with output data
  for (const connection of getOutputConnections(node)) {
    this.addNodeToBeExecuted(workflow, connection, nodeOutput);
  }
}
```

### Synaptiq: `WorkflowExecutor.execute()`
- **Topological order**: Pre-computes execution order via BFS, then iterates linearly
- **Context accumulation**: Each agent's output becomes the next agent's input context
- **SSE streaming**: Every state transition emits real-time events to the frontend
- **Persistence per-node**: Each node start/complete/error is persisted to MongoDB immediately
- **Dual mode**: `dry_run` simulates execution; normal mode calls actual LLMs
- **~300 lines** — dramatically simpler because agents handle complexity internally

```python
# Synaptiq execution loop (simplified)
for node_id in execution_order:
    yield SSEEvent("node_start", node_id)
    output = await _execute_node_with_llm(agent, accumulated_context, tenant_id)
    accumulated_context = output  # Chain outputs
    yield SSEEvent("node_complete", node_id)
    await _update_node_in_run(run_id, node_id, ...)
```

> [!WARNING]
> n8n's execution engine is **10x more complex** because it must handle arbitrary data transformations, fan-in/fan-out, loops, sub-workflows, error handling branches, and credential injection. Synaptiq's is simpler because the **intelligence is inside the agents** — the engine just orchestrates turns.

---

## 5. AI Integration Depth

### n8n's AI: Add-on, Not Core

n8n added AI capabilities as additional node types within their existing connector model:

| n8n AI Node | What It Does |
|---|---|
| **AI Agent** | Wrapper around LangChain agent — one node type |
| **AI Chain** | Sequential LLM calls with memory |
| **AI Memory** (Buffer/Window/Vector) | Connected as sub-nodes to agents |
| **AI Tool** | Wraps a tool as a sub-node |
| **AI Output Parser** | Structured output extraction |
| **AI Embeddings** | Vector embedding generation |
| **AI Vector Store** | Pinecone/Qdrant/etc. connectors |

**How it works**: You add an "AI Agent" node, then connect sub-nodes (memory, tools) to it via `ai_*` connection types. The agent is ONE node in a larger data pipeline.

```
[Trigger] → [HTTP Request] → [AI Agent + Memory + Tools] → [Slack] → [Airtable]
```

### Synaptiq's AI: The Foundation

Every workflow node IS an agent. There's no distinction between "AI nodes" and "regular nodes":

```
[Researcher Agent]     →  [Writer Agent]      →  [Editor Agent]
  ↳ system_prompt         ↳ system_prompt        ↳ system_prompt
  ↳ tools: [web_search]   ↳ tools: []            ↳ tools: []
  ↳ llm: gemini-2.5       ↳ llm: gemini-2.5      ↳ llm: gpt-4
```

Each node can:
- Have its own LLM model (per-node provider/model selection)
- Have its own system prompt defining personality and behavior
- Use tools autonomously (the agent decides when/how)
- Produce unbounded, creative output that feeds downstream agents

---

## 6. The Killer Differentiators

### 🔥 Differentiator 1: Natural Language → Workflow Generation

| | n8n | Synaptiq |
|---|---|---|
| **How workflows are created** | Manual drag-and-drop or JSON import | `"Build me a research pipeline"` → full spec generated |
| **LLM involvement in creation** | None (user designs manually) | LLM generates agent roles, edges, prompts, and routing |
| **Generation system** | N/A | `WORKFLOW_GENERATION_SYSTEM_PROMPT` → structured JSON spec |

Synaptiq's [workflow_service.py](file:///home/bharat/git/synaptiq/apps/backend/api/src/synaptiq_api/services/workflow_service.py#L232) `generate_workflow()` takes a natural language prompt and:
1. Calls an LLM with a structured generation prompt
2. Parses the JSON spec output
3. Validates it against `WorkflowSpec` schema
4. Auto-saves to MongoDB
5. Streams the result via SSE to render in real-time

**n8n has nothing equivalent.** Users must manually place nodes and configure connections.

---

### 🔥 Differentiator 2: Runtime Self-Modification

This is the **biggest architectural gap**:

| | n8n | Synaptiq |
|---|---|---|
| **Workflow mutability** | Static — fixed at design time | **Dynamic — agents can modify the workflow at runtime** |
| **`flow_type`** | N/A | `"static"` \| `"dynamic"` \| `"hybrid"` |
| **Manager Agent** | N/A | A special agent that can create/modify/rewrite the workflow graph mid-execution |

Synaptiq's `flow_type: "dynamic"` enables a **workflow manager agent** to:
- Add new agent nodes during execution
- Rewire edge connections based on intermediate results
- Spawn entirely new sub-workflows
- Adjust routing based on runtime conditions

This is fundamentally impossible in n8n because workflows are immutable data structures executed by a deterministic engine. The closest n8n gets is sub-workflow execution, but the graph shape is always fixed.

---

### 🔥 Differentiator 3: Per-Node Agent Autonomy

| | n8n | Synaptiq |
|---|---|---|
| **Node intelligence** | Configured parameters, deterministic | Each node is an autonomous LLM with its own system prompt |
| **Node output** | Structured JSON items | Free-form LLM-generated text/reasoning |
| **Tool usage** | User configures which tool → which input | Agent autonomously decides when/how to use tools |
| **Per-node LLM** | One AI agent config per workflow | Each node can use a **different LLM model** |

---

### 🔥 Differentiator 4: SSE Execution Observability

| | n8n | Synaptiq |
|---|---|---|
| **Execution visibility** | Post-execution log viewer | **Real-time SSE streaming** of every node transition |
| **Events** | Execution result after completion | `node_start` → `node_complete` → `execution_complete` |
| **Persistence** | Final result in DB | **Per-node** status updates persisted during execution |
| **Frontend viz** | Static execution view | Live node-by-node animation on the canvas |

---

## 7. What n8n Does Better (Honestly)

> [!CAUTION]
> These are areas where n8n has a significant advantage that Synaptiq should acknowledge:

| Capability | n8n | Synaptiq |
|---|---|---|
| **Integration breadth** | **400+ pre-built connectors** (Slack, GitHub, Google Sheets, Postgres, etc.) | Zero external integrations — agents must use generic tools |
| **Error handling** | Dedicated error trigger nodes, retry policies, fallback branches | Basic try/catch per node |
| **Sub-workflows** | First-class support — workflows can call other workflows | Not implemented |
| **Credential management** | Encrypted credential store with OAuth2 flows | Per-tenant BYOK keys only |
| **Webhooks & Triggers** | Cron, webhook, polling, event-based triggers | Manual execution or chat-initiated only |
| **Version control** | Workflow versioning, source control integration | No versioning |
| **Expressions** | Rich expression language for data transformation | No expression system |
| **Community** | 57K+ GitHub stars, active marketplace | Internal platform |
| **Partial execution** | Re-run from any node with cached upstream data | Full re-execution only |
| **Scale** | Proven at enterprise scale (queue mode, workers) | Single-process execution |
| **Binary data** | Native binary data support (files, images) | Text-only context passing |
| **Testing** | Built-in test mode, pinned data | Dry-run simulation only |

---

## 8. Strategic Positioning

```mermaid
quadchart
    title Workflow Platform Positioning
    x-axis "Integration Breadth" --> "AI Intelligence"
    y-axis "Static Workflows" --> "Adaptive Workflows"
    quadrant-1 "AI-Native Adaptive"
    quadrant-2 "Traditional Automation"
    quadrant-3 "Simple Integrations"
    quadrant-4 "AI-Enhanced Automation"
    "Synaptiq": [0.85, 0.90]
    "n8n": [0.75, 0.35]
    "Zapier": [0.80, 0.15]
    "LangGraph": [0.90, 0.70]
    "Make.com": [0.70, 0.20]
    "Temporal": [0.40, 0.40]
```

| Platform | Category |
|---|---|
| **Zapier / Make.com** | No-code integration automation |
| **n8n** | Code-optional integration automation + AI add-on |
| **Temporal / Conductor** | Durable workflow orchestration (infrastructure) |
| **LangGraph** | Code-first AI agent orchestration (library) |
| **Synaptiq** | AI-native workflow platform with NL generation + runtime mutation |

---

## 9. Architectural Overlap

Despite the fundamental differences, there ARE shared concepts:

| Shared Concept | n8n Implementation | Synaptiq Implementation |
|---|---|---|
| **DAG execution** | `WorkflowExecute` with node stack | `WorkflowExecutor` with topo sort |
| **Cycle handling** | `handleCycles()` utility | `visited` set in BFS |
| **Node status tracking** | `IRunData` per node | `NodeStatus` per node |
| **Conditional routing** | Switch/If nodes | `conditional_edges` with `condition_mapping` |
| **Visual canvas** | Vue 3 canvas editor | Angular canvas with SVG edges |
| **Multi-tenant** | Workspace isolation | `tenant_id` filtering |
| **Execution history** | Execution list view | `workflow_runs` collection |

---

## 10. Summary: Why Synaptiq Isn't "Just Another n8n"

```
n8n says:  "Connect your apps and automate data flows"
Synaptiq says: "Describe what you want → AI designs the team → agents collaborate → workflow evolves"
```

| Dimension | n8n | Synaptiq |
|---|---|---|
| **Workflow creation** | Human designs graph | **AI generates graph from NL** |
| **Node semantics** | Data transformer | **Autonomous agent** |
| **Runtime behavior** | Fixed pipeline | **Self-modifying graph** |
| **Context model** | Structured JSON items | **Accumulated conversation** |
| **AI depth** | Integration-level (call an API) | **Cognition-level (agent reasons)** |
| **Output nature** | Deterministic data | **Creative, reasoned output** |

> [!NOTE]
> **The clearest way to explain it**: n8n is like an assembly line where each station performs a fixed operation. Synaptiq is like a team meeting where each participant (agent) brings their expertise, builds on what others said, and the moderator (manager agent) can restructure the team mid-meeting if needed.
