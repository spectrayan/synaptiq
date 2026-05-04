# Context

- **Last Session**: May 3, 2026. Bootstrapped the Java/Spring alternative to the existing Python `agent-flow` library using Spring AI and Google ADK.
- **Current Focus**: Implementing the core engine and builder for `agent-flow-spring` library.
- **Key Decisions & Notes**: 
  - Using Google ADK 1.0.0 with Spring AI (`google-adk-spring-ai`).
  - DTOs exactly mirror the structure of `FlowSettings` from the Python agent flow library for seamless workflow definitions.
  - Streaming returns `Flux<Map<String, Object>>` matching the Python implementation's event yielding style.
- **Blockers & Open Questions**: 
  - Need to implement the actual logic connecting `FlowSettings` nodes to Google ADK `Agent` instances in `FlowBuilder`.
  - Encountered an issue downloading `spring-ai-core` milestone version during Maven build (need to resolve repo/version).
