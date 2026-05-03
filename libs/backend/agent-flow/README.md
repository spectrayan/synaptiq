# Agent Flow Engine

Dynamic JSON-based AI agent workflow engine for Synaptiq. Uses LangGraph for orchestration and supports `static`, `dynamic`, and `hybrid` flow types.

## Architecture

- **Builder** — Translates `FlowSpec` JSON into compiled LangGraph graphs
- **Coordinator** — Orchestrates multi-agent workflows with iteration-based planning/execution
- **Executor** — Provides the runtime API for starting and streaming workflow runs
- **Flow Generation** — LLM-powered workflow specification generation from natural language

## Flow Types

| Type | Description |
|------|-------------|
| `static` | Pre-defined agent graph with fixed edges |
| `dynamic` | LLM generates the workflow spec at runtime |
| `hybrid` | Static structure with dynamic sub-flows |

## Usage

```python
from agent_flow.builder.builder import build_workflow
from agent_flow.executor.engine import FlowExecutor

# Build from spec
graph, settings, ctx = build_workflow(spec_dict)

# Execute
executor = FlowExecutor(graph, settings)
async for event in executor.stream(input_data):
    print(event)
```
