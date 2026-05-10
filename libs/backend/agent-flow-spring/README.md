# agent-flow-spring

> Spring-based AI agent workflow engine for Synaptiq — orchestrates multi-agent flows with sequential, parallel, and supervisor patterns.

[![Part of Synaptiq](https://img.shields.io/badge/part%20of-Synaptiq-7C4DFF?style=flat-square)](../../../README.md)

## Overview

This library provides the core workflow execution engine for Synaptiq's multi-agent orchestration system. It enables users to design, save, and execute complex AI agent pipelines using a visual flow designer.

## Flow Types

| Type | Description |
|------|-------------|
| **Sequential** | Agents execute one after another, passing context forward |
| **Parallel** | Multiple agents execute concurrently, results are aggregated |
| **Supervisor** | A supervisor agent delegates work and synthesizes results |

## Key Components

| Component | Description |
|-----------|-------------|
| `WorkflowExecutor` | Core execution engine — resolves flow graphs and runs agents |
| `AgentRunner` | Executes individual agent nodes with LLM integration |
| `FlowParser` | Parses workflow definitions into executable graphs |
| `WorkflowRepository` | Persistence layer for workflow definitions and run history |

## Architecture

```
WorkflowController
    └── WorkflowUseCase (port)
        └── WorkflowService (domain)
            ├── WorkflowExecutor
            │   ├── SequentialStrategy
            │   ├── ParallelStrategy
            │   └── SupervisorStrategy
            └── AgentRunner
                └── Spring AI ChatClient
```

## Integration

This library is consumed by the `spring-apis` backend application as a Maven dependency. It is not deployed independently.

```xml
<dependency>
    <groupId>com.spectrayan.synaptiq</groupId>
    <artifactId>agent-flow-spring</artifactId>
    <version>${project.version}</version>
</dependency>
```
