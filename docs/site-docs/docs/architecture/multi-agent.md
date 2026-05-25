# Multi-Agent Orchestration

Synaptiq's workflow engine coordinates multiple specialized AI agents to solve complex problems. Built on the `agent-flow-spring` library, it supports sequential, parallel, supervisor, and dynamic flow patterns.

---

## Agent Model

Each agent in a workflow is configured with:

```yaml
Agent:
  id: "aba-specialist"
  name: "ABA Therapy Assistant"
  systemPrompt: |
    You are a Board Certified Behavior Analyst (BCBA) specializing in...
  model: "gemini-2.5-flash"
  temperature: 0.3
  maxTokens: 4096
  tools: []            # Optional MCP tools
  inputSchema: {}      # Expected input format
  outputSchema: {}     # Required output format
```

---

## Flow Types

### Sequential Flow

```mermaid
stateDiagram-v2
    [*] --> AgentA: Input
    AgentA --> AgentB: Output A
    AgentB --> AgentC: Output B
    AgentC --> [*]: Final Output
```

**Use case:** Linear pipelines where each step refines the previous output.

**Example:** Research → Analysis → Report Generation

```json
{
  "type": "SEQUENTIAL",
  "nodes": [
    { "id": "research", "systemPrompt": "Research the topic..." },
    { "id": "analysis", "systemPrompt": "Analyze the research findings..." },
    { "id": "report", "systemPrompt": "Generate a comprehensive report..." }
  ],
  "edges": [
    { "from": "research", "to": "analysis" },
    { "from": "analysis", "to": "report" }
  ]
}
```

### Parallel Flow

```mermaid
stateDiagram-v2
    [*] --> Fork
    Fork --> AgentA
    Fork --> AgentB
    Fork --> AgentC
    AgentA --> Join
    AgentB --> Join
    AgentC --> Join
    Join --> [*]: Merged Output
```

**Use case:** Independent analyses that can run concurrently.

### Supervisor Flow

```mermaid
stateDiagram-v2
    [*] --> Supervisor: Input
    Supervisor --> Delegate: Plan tasks
    Delegate --> SpecialistA
    Delegate --> SpecialistB
    Delegate --> SpecialistC
    SpecialistA --> Supervisor: Results
    SpecialistB --> Supervisor: Results
    SpecialistC --> Supervisor: Results
    Supervisor --> [*]: Synthesized Output
```

**Use case:** Complex multi-domain problems requiring cross-domain coordination.

**Example (Healthcare ABA):**

```json
{
  "type": "SUPERVISOR",
  "supervisor": {
    "id": "supervisor",
    "systemPrompt": "You are a clinical supervisor coordinating a multidisciplinary team..."
  },
  "specialists": [
    { "id": "aba", "systemPrompt": "You are a BCBA specializing in ABA therapy..." },
    { "id": "speech", "systemPrompt": "You are an SLP specializing in pediatric..." },
    { "id": "ot", "systemPrompt": "You are an OTR specializing in sensory..." },
    { "id": "cbt", "systemPrompt": "You are a licensed psychologist specializing in CBT..." }
  ]
}
```

### Dynamic Flow

```mermaid
stateDiagram-v2
    [*] --> Router: Input
    Router --> AgentA: Condition A
    Router --> AgentB: Condition B
    AgentA --> Router: May re-route
    AgentB --> Router: May re-route
    Router --> [*]: Complete
```

**Use case:** Adaptive workflows with conditional branching based on intermediate results.

---

## Execution Engine

The execution engine (`agent-flow-spring`) manages:

1. **Flow orchestration** — routes data between agents based on the flow type
2. **State management** — tracks execution state for each agent and the overall workflow
3. **Error handling** — retries, timeouts, and fallback agents
4. **Event emission** — SSE events for real-time UI updates

### Execution Lifecycle

```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> RUNNING: Execute
    RUNNING --> COMPLETED: All agents finish
    RUNNING --> FAILED: Error + no retry
    RUNNING --> RUNNING: Agent completes, next starts
    FAILED --> [*]
    COMPLETED --> [*]
```

### Event Types

| Event | Description |
|-------|-------------|
| `workflow_start` | Workflow execution begins |
| `agent_start` | Individual agent begins processing |
| `agent_output` | Agent produces output |
| `agent_complete` | Agent finishes successfully |
| `agent_error` | Agent encounters an error |
| `workflow_complete` | All agents finished, final output ready |
| `workflow_error` | Workflow failed |

---

## Inter-Agent Communication

Agents communicate through structured data contracts:

```mermaid
flowchart LR
    A["Agent A<br/>Output: JSON"] -->|"Structured handoff"| B["Agent B<br/>Input: JSON"]
    B -->|"Structured handoff"| C["Agent C<br/>Input: JSON"]
```

The supervisor agent can:
- **Merge** outputs from parallel agents
- **Resolve conflicts** between contradictory specialist recommendations
- **Request clarification** by re-invoking a specialist with additional context
- **Prioritize** based on domain importance or client preferences
