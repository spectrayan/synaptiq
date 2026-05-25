# ADR-005: Multi-Agent Orchestration

**Status:** Accepted  
**Authors:** Spectrayan Team  
**Date:** 2025-07-15

---

## Context

Complex business processes (e.g., ABA therapy goal generation) require multiple specialized AI agents working together. We need an orchestration engine that supports various coordination patterns.

## Decision

Build `agent-flow-spring` as a reusable library supporting four flow types:

| Type | Pattern | Spring AI Integration |
|------|---------|----------------------|
| **Sequential** | Chain of agents | `ChatClient` chaining with `Advisor` pipeline |
| **Parallel** | Concurrent agents + merge | `Flux.merge()` with reactive streams |
| **Supervisor** | Coordinator + specialists | Supervisor uses tool-calling to invoke specialists |
| **Dynamic** | Runtime routing | Agent output determines next agent via routing function |

### Supervisor Implementation

The supervisor agent is implemented using Spring AI's **tool calling**:

1. Supervisor receives the task and specialist descriptions
2. Supervisor decides which specialists to invoke (via function calls)
3. Each specialist runs with its own system prompt and model config
4. Supervisor receives all specialist outputs and synthesizes the final result

## Consequences

- **Positive:** Flexible — covers most multi-agent patterns
- **Positive:** Reusable — `agent-flow-spring` is a standalone library
- **Positive:** Observable — event-driven execution with SSE status updates
- **Negative:** Supervisor flow has higher latency (sequential specialist calls)
- **Negative:** Dynamic flow debugging is complex
