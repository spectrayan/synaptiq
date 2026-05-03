from __future__ import annotations

from typing import Any, Dict

from langchain_core.language_models.chat_models import BaseChatModel
from langchain_core.runnables import RunnableConfig

from ..models.settings import LLMSettings, Provider


class LLMFactory:
    """Factory to construct LangChain chat models from LLMSettings.

    Only initializes providers based on configuration to avoid hard-coded values.
    """

    @staticmethod
    def create(settings: LLMSettings) -> BaseChatModel:
        provider = settings.provider
        if provider == Provider.vertexai:
            # Lazy import to avoid hard dependency when not used
            from langchain_google_vertexai import ChatVertexAI

            kwargs: Dict[str, Any] = {
                "model": settings.model,
                "temperature": settings.temperature,
                "streaming": settings.streaming,
            }
            if settings.max_tokens is not None:
                kwargs["max_tokens"] = settings.max_tokens
            # Provider specific extras e.g., location, project
            kwargs.update(settings.params or {})
            return ChatVertexAI(**kwargs)

        if provider == Provider.openai:
            from langchain_openai import ChatOpenAI

            kwargs = {
                "model": settings.model,
                "temperature": settings.temperature,
                "streaming": settings.streaming,
            }
            if settings.max_tokens is not None:
                kwargs["max_tokens"] = settings.max_tokens
            kwargs.update(settings.params or {})
            return ChatOpenAI(**kwargs)

        if provider == Provider.anthropic:
            from langchain_anthropic import ChatAnthropic

            kwargs = {
                "model": settings.model,
                "temperature": settings.temperature,
                "streaming": settings.streaming,
            }
            if settings.max_tokens is not None:
                kwargs["max_tokens"] = settings.max_tokens
            kwargs.update(settings.params or {})
            return ChatAnthropic(**kwargs)

        raise ValueError(f"Unsupported provider: {provider}")
