# @synaptiq/chat

> Chat UI components and streaming utilities for the Synaptiq conversational interface.

[![Part of Synaptiq](https://img.shields.io/badge/part%20of-Synaptiq-7C4DFF?style=flat-square)](../../../README.md)

## Overview

This library provides the core chat UI building blocks used by the Synaptiq shell application. It includes message rendering, streaming SSE support, typing indicators, and suggestion chips.

## Components

| Component | Description |
|-----------|-------------|
| `ChatMessageListComponent` | Renders a scrollable list of chat messages with auto-scroll |
| `ChatInputBarComponent` | Input field with send button, suggestion chips, and file upload |
| `ChatBubbleComponent` | Individual message bubble with markdown rendering and DSL embedding |

## Features

- **SSE Streaming** — Real-time message streaming with token-by-token rendering
- **Markdown Support** — Full markdown rendering in assistant messages
- **DSL Integration** — Embeds rich components (cards, grids, tables) inline in messages
- **Auto-Scroll** — Smooth scrolling with intelligent scroll-lock behavior
- **Typing Indicator** — Animated dot indicator during AI response generation

## Building

```bash
pnpm nx build chat
```

## Running Tests

```bash
pnpm nx test chat
```
