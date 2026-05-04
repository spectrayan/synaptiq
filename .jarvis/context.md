# Context

- **Last Session**: May 3, 2026. Added numeric parsing, 10MB file upload limits (converted to base64) to the Workflow Run Dialog and resolved Angular compiler warnings (`NG8107`, `NG8011`) in the DSL renderer templates.
- **Current Focus**: Enhancing Workflow Execution UI and migrating Agent Flow to Spring Boot.
- **Key Decisions & Notes**: 
  - Workflow file inputs are read and converted to Base64 strings directly on the client side before being submitted.
  - Using Google ADK 1.0.0 with Spring AI (`google-adk-spring-ai`).
  - DTOs exactly mirror the structure of `FlowSettings` from the Python agent flow library for seamless workflow definitions.
  - Streaming returns `Flux<Map<String, Object>>` matching the Python implementation's event yielding style.
- **Blockers & Open Questions**: 
  - Need to implement the actual logic connecting `FlowSettings` nodes to Google ADK `Agent` instances in `FlowBuilder`.
  - Encountered an issue downloading `spring-ai-core` milestone version during Maven build (need to resolve repo/version).
