# Changelog

## May 3, 2026
- Added robust input validation (10MB file size limit) and base64 file upload support to `WorkflowRunDialogComponent`.
- Fixed Angular compiler warnings (`NG8107`, `NG8011`) in `dsl-renderer` library templates.
- Bootstrapped `agent-flow-spring` Maven module.
- Generated Java DTOs matching Python `settings.py` workflow specifications.
- Skeletoned the `FlowExecutor` and `AgentEngineApp` to handle run state and streaming of events via Project Reactor `Flux`.
