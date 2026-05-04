# Task List

## In Progress

### Agent Flow Spring Boot Library
- **Status**: In Progress
- **Branch**: `main`
- **What was done**: Created `agent-flow-spring` Maven lib under `libs/backend`. Added dependencies for Spring Boot 4, Spring AI, and Google ADK. Mapped Python flow spec models to Java DTOs (`FlowSettings`, `AgentSettings`, etc.). Created `FlowExecutor`, `RunState`, and `AgentEngineApp` execution framework.
- **Next steps**: Complete `FlowBuilder.java` to map `FlowSettings` JSON into actual Google ADK graph nodes (`LoopAgent`, `SequentialAgent`). Fix Maven dependency resolution for `spring-ai-core:2.0.0-M4` or ensure the milestone repos work correctly during compilation.
- **Files touched**: `libs/backend/agent-flow-spring/pom.xml`, `libs/backend/agent-flow-spring/src/main/java/com/synaptiq/agentflow/` (various model and executor classes).

## Up Next
- Implement actual `FlowBuilder` logic.
- Integrate `agent-flow-spring` into the backend API.

## Recently Completed
(None for this session)
