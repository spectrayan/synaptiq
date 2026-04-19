"""LLM Provider abstraction — pluggable adapters for LLM inference (T6.1–T6.7).

Supports:
  - Gemini (Vertex AI) — default platform-managed provider
  - OpenAI (GPT-4o / GPT-4o-mini) — BYOK
  - Circuit breaker pattern (50% error / 30s window → open; 60s cooldown)
  - Keyword search fallback when circuit is open
"""
import asyncio
import json
import logging
import time
from abc import ABC, abstractmethod
from collections import deque
from typing import AsyncIterator

from synaptiq_api.core.config import settings

logger = logging.getLogger(__name__)


# ---------------------------------------------------------------------------
# T6.1 — Abstract LLM provider interface
# ---------------------------------------------------------------------------

class LLMProvider(ABC):
    """Abstract interface for all LLM providers."""

    @abstractmethod
    async def stream_chat(
        self,
        messages: list[dict],
        system_prompt: str,
        model_id: str = "",
        temperature: float = 0.7,
        max_tokens: int = 2048,
    ) -> AsyncIterator[str]:
        """
        Stream chat completion tokens.

        Args:
            messages: Conversation history [{role, content}, ...].
            system_prompt: Compiled system prompt for this tenant.
            model_id: Specific model override (empty = provider default).
            temperature: Sampling temperature.
            max_tokens: Max output tokens.

        Yields:
            str: Individual tokens/chunks as they arrive.
        """
        ...

    @abstractmethod
    async def count_tokens(self, text: str) -> int:
        """Estimate token count for input text."""
        ...


# ---------------------------------------------------------------------------
# T6.2 — Gemini (Vertex AI) Adapter
# ---------------------------------------------------------------------------

class GeminiAdapter(LLMProvider):
    """Google Gemini via google-genai SDK (REQ-AI-P3).

    Uses API key (generativelanguage) when available, falls back to Vertex AI ADC.
    """

    async def stream_chat(
        self,
        messages: list[dict],
        system_prompt: str,
        model_id: str = "",
        temperature: float = 0.7,
        max_tokens: int = 2048,
    ) -> AsyncIterator[str]:
        from google import genai
        from google.genai import types

        model = model_id or settings.gemini_model

        # Prefer API key (Google AI Studio); fall back to Vertex AI ADC
        if settings.gemini_api_key:
            client = genai.Client(api_key=settings.gemini_api_key)
        else:
            client = genai.Client(
                vertexai=True,
                project=settings.vertexai_project,
                location=settings.vertexai_location,
            )

        # Convert messages to Gemini Content format
        contents = []
        for msg in messages:
            role = "user" if msg["role"] == "user" else "model"
            contents.append(types.Content(
                role=role,
                parts=[types.Part.from_text(text=msg["content"])],
            ))

        config = types.GenerateContentConfig(
            system_instruction=system_prompt,
            temperature=temperature,
            max_output_tokens=max_tokens,
        )

        response_stream = await client.aio.models.generate_content_stream(
            model=model,
            contents=contents,
            config=config,
        )
        async for chunk in response_stream:
            if chunk.text:
                yield chunk.text

    async def count_tokens(self, text: str) -> int:
        # Rough estimate: ~4 chars per token for multilingual
        return len(text) // 4


# ---------------------------------------------------------------------------
# T6.3 — OpenAI Adapter
# ---------------------------------------------------------------------------

class OpenAIAdapter(LLMProvider):
    """OpenAI GPT-4o / GPT-4o-mini (REQ-AI-P3). Requires BYOK key."""

    DEFAULT_MODEL = "gpt-4o-mini"

    def __init__(self, api_key: str = ""):
        self._api_key = api_key or settings.openai_api_key

    async def stream_chat(
        self,
        messages: list[dict],
        system_prompt: str,
        model_id: str = "",
        temperature: float = 0.7,
        max_tokens: int = 2048,
    ) -> AsyncIterator[str]:
        from openai import AsyncOpenAI

        client = AsyncOpenAI(api_key=self._api_key)
        model = model_id or self.DEFAULT_MODEL

        full_messages = [{"role": "system", "content": system_prompt}]
        full_messages.extend(messages)

        stream = await client.chat.completions.create(
            model=model,
            messages=full_messages,
            temperature=temperature,
            max_tokens=max_tokens,
            stream=True,
        )

        async for chunk in stream:
            delta = chunk.choices[0].delta if chunk.choices else None
            if delta and delta.content:
                yield delta.content

    async def count_tokens(self, text: str) -> int:
        # GPT-4o: ~4 chars per token
        return len(text) // 4


# ---------------------------------------------------------------------------
# T6.3b — Dev Echo Adapter (local development without LLM keys)
# ---------------------------------------------------------------------------

class DevEchoAdapter(LLMProvider):
    """
    Development-only adapter that echoes contextual responses without real LLM calls.

    Used automatically when no LLM API keys are configured (VERTEXAI_PROJECT
    and OPENAI_API_KEY are both empty). Streams a helpful response that
    exercises the full SSE pipeline including DSL component rendering.
    """

    async def stream_chat(
        self,
        messages: list[dict],
        system_prompt: str,
        model_id: str = "",
        temperature: float = 0.7,
        max_tokens: int = 2048,
    ) -> AsyncIterator[str]:
        import asyncio

        # Extract the user's last message
        user_msg = ""
        for msg in reversed(messages):
            if msg.get("role") == "user":
                user_msg = msg.get("content", "")
                break

        # Build a contextual echo response
        response_parts = [
            f"🔬 **Dev Mode** — I received your message: *\"{user_msg[:120]}\"*\n\n",
            "I'm running in **echo mode** because no LLM provider is configured. ",
            "To enable real AI responses, set `VERTEXAI_PROJECT` or `OPENAI_API_KEY` in your `.env` file.\n\n",
            "Here's what the full pipeline would do:\n",
            "1. **Embed** your query into a vector\n",
            "2. **Search** the catalog using vector similarity\n",
            "3. **Generate** a contextual response with product recommendations\n",
            "4. **Render** interactive DSL components\n\n",
            "```component\n",
            '{"type": "item_card", "item": {"name": "Sample Product", "description": "This is a demo product rendered by the DSL pipeline.", "price": 49.99, "currency": "USD", "rating": 4.5, "image_url": ""}, "actions": [{"label": "View Details", "action": "view_item", "payload": {"item_id": "demo-001"}}]}\n',
            "```\n",
        ]

        # Stream token by token with realistic delay
        for part in response_parts:
            # Split into small chunks to simulate token streaming
            words = part.split(" ")
            for i, word in enumerate(words):
                token = word if i == 0 else f" {word}"
                yield token
                await asyncio.sleep(0.02)  # 20ms per token

    async def count_tokens(self, text: str) -> int:
        return len(text) // 4


# ---------------------------------------------------------------------------
# T6.4 — Platform Managed Adapter (default, uses platform key)
# ---------------------------------------------------------------------------

class PlatformManagedAdapter(LLMProvider):
    """
    Default adapter — uses the platform's own API key (REQ-AI-P1).
    Routes to Gemini by default, configurable via settings.

    Falls back to DevEchoAdapter when no LLM credentials are configured
    (local development without API keys).
    """

    def __init__(self):
        provider = settings.default_llm_provider

        if provider == "openai" and settings.openai_api_key:
            self._delegate = OpenAIAdapter(api_key=settings.openai_api_key)
        elif provider == "vertexai" and settings.vertexai_project:
            self._delegate = GeminiAdapter()
        elif settings.openai_api_key:
            self._delegate = OpenAIAdapter(api_key=settings.openai_api_key)
        elif settings.vertexai_project:
            self._delegate = GeminiAdapter()
        else:
            logger.warning(
                "No LLM credentials configured — using DevEchoAdapter. "
                "Set VERTEXAI_PROJECT or OPENAI_API_KEY for real AI responses."
            )
            self._delegate = DevEchoAdapter()

    async def stream_chat(self, *args, **kwargs) -> AsyncIterator[str]:
        async for token in self._delegate.stream_chat(*args, **kwargs):
            yield token

    async def count_tokens(self, text: str) -> int:
        return await self._delegate.count_tokens(text)


# ---------------------------------------------------------------------------
# T6.6 — Circuit Breaker
# ---------------------------------------------------------------------------

class CircuitBreaker:
    """
    Circuit breaker for LLM calls (REQ-NF-CB1–CB3).

    States: CLOSED (normal) → OPEN (failing) → HALF_OPEN (probing)
    Opens when error rate exceeds 50% in a 30s window.
    Cooldown: 60 seconds before half-open probe.
    """

    CLOSED = "closed"
    OPEN = "open"
    HALF_OPEN = "half_open"

    def __init__(
        self,
        error_threshold: float = 0.5,
        window_seconds: int = 30,
        cooldown_seconds: int = 60,
        min_calls: int = 5,
    ):
        self.error_threshold = error_threshold
        self.window_seconds = window_seconds
        self.cooldown_seconds = cooldown_seconds
        self.min_calls = min_calls

        self.state = self.CLOSED
        self._calls: deque[tuple[float, bool]] = deque()  # (timestamp, is_error)
        self._opened_at: float = 0

    def _prune(self):
        cutoff = time.time() - self.window_seconds
        while self._calls and self._calls[0][0] < cutoff:
            self._calls.popleft()

    def record_success(self):
        self._calls.append((time.time(), False))
        if self.state == self.HALF_OPEN:
            self.state = self.CLOSED
            logger.info("Circuit breaker → CLOSED (probe succeeded)")

    def record_failure(self):
        self._calls.append((time.time(), True))
        self._check_threshold()

    def _check_threshold(self):
        self._prune()
        total = len(self._calls)
        if total < self.min_calls:
            return
        errors = sum(1 for _, err in self._calls if err)
        if errors / total >= self.error_threshold:
            self.state = self.OPEN
            self._opened_at = time.time()
            logger.warning(
                "Circuit breaker → OPEN (%.0f%% errors in %ds window)",
                (errors / total) * 100, self.window_seconds,
            )

    @property
    def is_open(self) -> bool:
        if self.state == self.CLOSED:
            return False
        if self.state == self.OPEN:
            elapsed = time.time() - self._opened_at
            if elapsed >= self.cooldown_seconds:
                self.state = self.HALF_OPEN
                logger.info("Circuit breaker → HALF_OPEN (cooldown elapsed)")
                return False  # Allow one probe
            return True
        return False  # HALF_OPEN allows through


# ---------------------------------------------------------------------------
# Factory — resolve adapter from tenant config
# ---------------------------------------------------------------------------

# Global circuit breakers per tenant
_circuit_breakers: dict[str, CircuitBreaker] = {}


def get_circuit_breaker(tenant_id: str) -> CircuitBreaker:
    if tenant_id not in _circuit_breakers:
        _circuit_breakers[tenant_id] = CircuitBreaker()
    return _circuit_breakers[tenant_id]


def get_provider(llm_config: dict, byok_key: str = "") -> LLMProvider:
    """
    Resolve the LLM provider adapter from tenant config (T6.4, T6.5).

    Args:
        llm_config: Tenant's LLMProviderConfig dict.
        byok_key: Decrypted BYOK API key (empty = platform managed).
    """
    provider_type = llm_config.get("provider", "platform_managed")

    if provider_type == "openai" and byok_key:
        return OpenAIAdapter(api_key=byok_key)
    elif provider_type == "gemini":
        return GeminiAdapter()
    else:
        return PlatformManagedAdapter()
