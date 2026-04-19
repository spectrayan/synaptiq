"""Chat orchestration service — the core AI pipeline (T6.8–T6.11).

Pipeline: validate → rate limit → load session → build system prompt
→ embed query → vector search → inject context → LLM stream → parse DSL
→ log turn → write usage.
"""
import json
import logging
import time
import uuid
from datetime import datetime
from typing import Any, AsyncIterator

from synaptiq_api.core.config import settings
from synaptiq_api.core.mongodb import get_db
from synaptiq_api.core.redis import get_redis
from synaptiq_api.services.embedding_service import generate_embedding
from synaptiq_api.services.llm_provider import (
    CircuitBreaker,
    get_circuit_breaker,
    get_provider,
)
from synaptiq_api.services.prompt_service import build_system_prompt
from synaptiq_api.services.search_service import catalog_search, keyword_search

logger = logging.getLogger(__name__)

# Maximum conversation history turns to keep in context
MAX_HISTORY_TURNS = 20
USAGE_COLLECTION = "usage_ledger"
SESSIONS_COLLECTION = "sessions"


# ---------------------------------------------------------------------------
# SSE Event helpers
# ---------------------------------------------------------------------------

def _sse_event(event: str, data: Any) -> str:
    """Format a Server-Sent Event."""
    payload = json.dumps(data) if not isinstance(data, str) else data
    return f"event: {event}\ndata: {payload}\n\n"


def _sse_token(token: str) -> str:
    return _sse_event("token", {"text": token})


def _sse_component(component: dict) -> str:
    return _sse_event("component", component)


def _sse_done(turn_id: str, tokens_in: int, tokens_out: int) -> str:
    return _sse_event("done", {
        "turn_id": turn_id,
        "tokens_input": tokens_in,
        "tokens_output": tokens_out,
    })


def _sse_error(message: str) -> str:
    return _sse_event("error", {"message": message})


# ---------------------------------------------------------------------------
# T6.8 — Main chat stream pipeline
# ---------------------------------------------------------------------------

async def chat_stream(
    tenant_id: str,
    session_id: str,
    user_message: str,
    model_override: str | None = None,
) -> AsyncIterator[str]:
    """
    Full chat pipeline yielding SSE events (REQ-C7, REQ-AI11–AI16, REQ-NF2).

    Steps:
      1. Load/create session
      2. Rate limit check
      3. Build system prompt
      4. Embed query → vector search (or keyword fallback)
      5. Inject search results as context
      6. Stream LLM response
      7. Parse any DSL components from output
      8. Log turn + usage
    """
    turn_id = str(uuid.uuid4())
    start_time = time.time()
    db = get_db()

    # -- Step 1: Load session & history
    session = await _load_or_create_session(tenant_id, session_id)
    history = _build_history(session.get("turns", []))

    # -- Step 2: Rate limit
    rate_ok = await _check_rate_limit(tenant_id, session_id)
    if not rate_ok:
        yield _sse_error("Rate limit exceeded. Please wait before sending another message.")
        return

    # -- Step 3: Build system prompt (cached)
    system_prompt = await build_system_prompt(tenant_id)

    # -- Step 4: Search for relevant catalog items
    cb = get_circuit_breaker(tenant_id)
    search_results: list[dict] = []

    if cb.is_open:
        # T6.7 — keyword fallback
        logger.warning("Circuit open for %s — using keyword fallback", tenant_id)
        search_results = await keyword_search(tenant_id, user_message, top_k=5)
        yield _sse_event("status", {"message": "Using keyword search (AI temporarily unavailable)"})
    else:
        try:
            query_embedding = await generate_embedding(user_message)
            if query_embedding:
                search_results = await catalog_search(
                    tenant_id=tenant_id,
                    query_embedding=query_embedding,
                    top_k=8,
                    min_score=0.3,
                )
        except Exception as e:
            logger.warning("Embedding/search failed: %s — proceeding without context", e)

    # -- Step 5: Build LLM messages with search context
    context_msg = _build_context_message(search_results)
    messages = history.copy()
    if context_msg:
        messages.append({"role": "user", "content": context_msg})
    messages.append({"role": "user", "content": user_message})

    # -- Step 6: Stream LLM
    tenant = await db["tenants"].find_one({"tenant_id": tenant_id})
    llm_config = (tenant.get("config", {}) if tenant else {}).get("llm_provider", {})
    provider = get_provider(llm_config)
    model_id = model_override or llm_config.get("model_id", "")

    full_response = ""
    tokens_out = 0

    try:
        async for token in provider.stream_chat(
            messages=messages,
            system_prompt=system_prompt,
            model_id=model_id,
        ):
            full_response += token
            tokens_out += 1
            yield _sse_token(token)
        cb.record_success()

    except Exception as e:
        cb.record_failure()
        logger.error("LLM stream error for tenant %s: %s", tenant_id, e)

        if cb.is_open:
            # Fallback: return search results as structured data
            if search_results:
                component = {
                    "type": "item_grid",
                    "items": [
                        _sanitize_item(item) for item in search_results[:6]
                    ],
                    "columns": 3,
                }
                yield _sse_component(component)
                full_response = "[Circuit open — displayed search results]"
            else:
                yield _sse_error("I'm having trouble responding right now. Please try again.")
                return
        else:
            yield _sse_error("Something went wrong. Please try again.")
            return

    # -- Step 7: Parse DSL components from response and get cleaned text
    components, cleaned_text = _extract_components(full_response)
    if components:
        # Emit a text replacement so the frontend swaps out the raw JSON
        yield _sse_event("text_replace", {"text": cleaned_text})
        for comp in components:
            yield _sse_component(comp)

    # -- Step 8: Calculate metrics and finalize
    latency_ms = int((time.time() - start_time) * 1000)
    tokens_in = await provider.count_tokens(
        system_prompt + " ".join(m["content"] for m in messages),
    )

    yield _sse_done(turn_id, tokens_in, tokens_out)

    # -- Step 9: Async persist (fire-and-forget)
    import asyncio
    asyncio.create_task(_persist_turn(
        tenant_id=tenant_id,
        session_id=session_id,
        turn_id=turn_id,
        user_message=user_message,
        assistant_response=cleaned_text if components else full_response,
        components=components,
        tokens_in=tokens_in,
        tokens_out=tokens_out,
        model_id=model_id or provider.__class__.__name__,
        latency_ms=latency_ms,
    ))


# ---------------------------------------------------------------------------
# Session helpers (T6.12–T6.14)
# ---------------------------------------------------------------------------

async def _load_or_create_session(tenant_id: str, session_id: str) -> dict:
    """Load an existing session or create a new one."""
    db = get_db()
    session = await db[SESSIONS_COLLECTION].find_one({
        "session_id": session_id,
        "tenant_id": tenant_id,
    })

    if not session:
        session = {
            "session_id": session_id,
            "tenant_id": tenant_id,
            "turns": [],
            "active_filters": [],
            "created_at": datetime.utcnow(),
            "updated_at": datetime.utcnow(),
        }
        await db[SESSIONS_COLLECTION].insert_one(session)
        logger.info("Created new session %s for tenant %s", session_id, tenant_id)

    return session


def _build_history(turns: list[dict]) -> list[dict]:
    """Convert stored turns to LLM message format, keeping last N turns."""
    recent = turns[-MAX_HISTORY_TURNS:]
    messages = []
    for turn in recent:
        role = turn.get("role", "user")
        content = turn.get("content", "")
        if content and role in ("user", "assistant"):
            messages.append({"role": role, "content": content})
    return messages


# ---------------------------------------------------------------------------
# Context injection
# ---------------------------------------------------------------------------

def _build_context_message(results: list[dict]) -> str:
    """Build a context injection message from search results."""
    if not results:
        return ""

    items_text = []
    for i, item in enumerate(results, 1):
        data = item.get("data", {})
        score = item.get("score", 0)
        fields = " | ".join(f"{k}: {v}" for k, v in data.items() if v)
        items_text.append(f"[{i}] (score: {score:.2f}) {fields}")

    return (
        "[SYSTEM: The following catalog items are relevant to the user's query. "
        "Use these to formulate your response. Cite item numbers when referencing items.]\n\n"
        + "\n".join(items_text)
    )


# ---------------------------------------------------------------------------
# DSL Component extraction
# ---------------------------------------------------------------------------

import re

# Known DSL component types
_COMPONENT_TYPES = {
    "item_card", "item_grid", "item_detail", "comparison_table",
    "filter_summary", "result_count", "empty_state", "action_confirm",
    "info_banner", "data_table", "form_input",
}


def _extract_components(response: str) -> tuple[list[dict], str]:
    """
    Extract DSL components from LLM output and return cleaned text.

    Handles multiple LLM output patterns:
      1. ```component ... ```  (instructed format)
      2. ```json ... ```       (common LLM fallback)
      3. Bare JSON objects     (no fences at all)

    Returns:
        (components, cleaned_text) — components list and the text with
        component JSON blocks stripped out for clean display.
    """
    components: list[dict] = []
    cleaned = response

    # Pattern 1 & 2: Extract from code fences (```component or ```json)
    fence_pattern = re.compile(
        r"```(?:component|json)\s*\n(.*?)```",
        re.DOTALL,
    )

    for match in fence_pattern.finditer(response):
        raw = match.group(1).strip()
        try:
            comp = json.loads(raw)
            if isinstance(comp, dict) and comp.get("type") in _COMPONENT_TYPES:
                components.append(comp)
                cleaned = cleaned.replace(match.group(0), "")
        except json.JSONDecodeError:
            logger.debug("Failed to parse fenced JSON: %s", raw[:100])

    # Pattern 3: Bare JSON objects with a "type" field (no fences)
    # Only try this if no fenced components were found
    if not components:
        # Match JSON objects that start with {"type": "component_type"
        bare_pattern = re.compile(
            r'\{[^{}]*"type"\s*:\s*"(' + "|".join(_COMPONENT_TYPES) + r')"[^{}]*(?:\{[^{}]*\}[^{}]*)*\}',
        )
        for match in bare_pattern.finditer(response):
            raw = match.group(0)
            try:
                comp = json.loads(raw)
                if isinstance(comp, dict) and comp.get("type") in _COMPONENT_TYPES:
                    components.append(comp)
                    cleaned = cleaned.replace(raw, "")
            except json.JSONDecodeError:
                pass

    # Clean up resulting text: remove excess whitespace/newlines
    cleaned = re.sub(r"\n{3,}", "\n\n", cleaned).strip()

    # Normalize all components to ensure correct CatalogItemData structure
    components = [_normalize_component(c) for c in components]

    return components, cleaned


def _normalize_component(comp: dict) -> dict:
    """
    Normalize a component to ensure CatalogItemData items have the correct
    nested {item_id, data} structure.

    LLMs frequently produce flat items like:
      {"item_id": "x", "name": "Foo", "price": 99}
    instead of the required:
      {"item_id": "x", "data": {"name": "Foo", "price": 99}}

    This function fixes that.
    """
    comp_type = comp.get("type", "")

    # Components that have a single 'item' field
    if comp_type in ("item_card", "item_detail"):
        item = comp.get("item")
        if isinstance(item, dict):
            comp["item"] = _normalize_item(item)

    # Components that have an 'items' array
    if comp_type in ("item_grid", "comparison_table"):
        items = comp.get("items", [])
        if isinstance(items, list):
            comp["items"] = [_normalize_item(it) for it in items if isinstance(it, dict)]

    return comp


def _normalize_item(item: dict) -> dict:
    """
    Ensure an item has the correct CatalogItemData shape:
      {item_id: string, data: Record<string, unknown>}

    If the LLM placed fields flat on the item object, move them into 'data'.
    """
    # Already properly structured
    if "data" in item and isinstance(item["data"], dict) and item["data"]:
        return item

    # Fields that are NOT part of the data payload
    reserved_keys = {"item_id", "data", "status", "score", "type"}

    # Extract item_id
    item_id = str(item.get("item_id", item.get("id", item.get("_id", ""))))
    if not item_id:
        # Generate a stable ID from the first string value
        for v in item.values():
            if isinstance(v, str) and v:
                import hashlib
                item_id = hashlib.md5(v.encode()).hexdigest()[:12]
                break

    # Collect all non-reserved fields into 'data'
    data = {}
    for k, v in item.items():
        if k not in reserved_keys:
            data[k] = v

    # If 'data' existed but was empty, and we found flat fields, use those
    if not data and "data" in item and isinstance(item.get("data"), dict):
        data = item["data"]

    return {
        "item_id": item_id,
        "data": data,
        "status": item.get("status", "active"),
    }


def _sanitize_item(item: dict) -> dict:
    """Sanitize a catalog item for DSL component output."""
    return {
        "item_id": str(item.get("_id", "")),
        "data": item.get("data", {}),
        "status": item.get("status", "active"),
    }


# ---------------------------------------------------------------------------
# Rate limiting
# ---------------------------------------------------------------------------

async def _check_rate_limit(tenant_id: str, session_id: str) -> bool:
    """
    Simple sliding-window rate limiter using Redis (REQ-NF-RL1).

    Limits: 30 messages per minute per session.
    """
    try:
        redis = get_redis()
        key = f"rl:{tenant_id}:{session_id}"
        now = time.time()

        pipe = redis.pipeline()
        pipe.zremrangebyscore(key, 0, now - 60)
        pipe.zcard(key)
        pipe.zadd(key, {str(now): now})
        pipe.expire(key, 120)
        results = await pipe.execute()

        count = results[1]
        if count >= 30:
            logger.warning("Rate limit hit: %s/%s (%d/min)", tenant_id, session_id, count)
            return False
        return True
    except Exception:
        logger.warning("Rate limit check failed, allowing through")
        return True


# ---------------------------------------------------------------------------
# T6.10, T6.11 — Persist turn + usage ledger
# ---------------------------------------------------------------------------

async def _persist_turn(
    tenant_id: str,
    session_id: str,
    turn_id: str,
    user_message: str,
    assistant_response: str,
    components: list[dict],
    tokens_in: int,
    tokens_out: int,
    model_id: str,
    latency_ms: int,
) -> None:
    """Write conversation turn to session and usage event to ledger."""
    db = get_db()
    now = datetime.utcnow()

    # Append turns to session
    user_turn = {
        "turn_id": turn_id,
        "role": "user",
        "content": user_message,
        "created_at": now,
    }
    assistant_turn = {
        "turn_id": turn_id,
        "role": "assistant",
        "content": assistant_response,
        "ui_components": components if components else None,
        "token_count_input": tokens_in,
        "token_count_output": tokens_out,
        "model_id": model_id,
        "latency_ms": latency_ms,
        "created_at": now,
    }

    await db[SESSIONS_COLLECTION].update_one(
        {"session_id": session_id, "tenant_id": tenant_id},
        {
            "$push": {
                "turns": {"$each": [user_turn, assistant_turn]},
            },
            "$set": {"updated_at": now},
        },
    )

    # T6.11 — Append to usage ledger (fire-and-forget)
    await db[USAGE_COLLECTION].insert_one({
        "tenant_id": tenant_id,
        "session_id": session_id,
        "event_type": "token_consumption",
        "tokens_input": tokens_in,
        "tokens_output": tokens_out,
        "model_id": model_id,
        "provider": model_id.split("-")[0] if model_id else "platform",
        "estimated_cost_usd": _estimate_cost(tokens_in, tokens_out, model_id),
        "created_at": now,
    })

    logger.info(
        "Turn %s persisted: %d in / %d out tokens, %dms",
        turn_id, tokens_in, tokens_out, latency_ms,
    )


def _estimate_cost(tokens_in: int, tokens_out: int, model_id: str) -> float:
    """
    Rough cost estimation per model (REQ-PR6).

    Prices as of 2025 (per 1M tokens):
      - gemini-2.0-flash: $0.10 input, $0.40 output
      - gpt-4o-mini: $0.15 input, $0.60 output
      - gpt-4o: $2.50 input, $10.00 output
    """
    rates = {
        "gemini": (0.10, 0.40),
        "gpt-4o-mini": (0.15, 0.60),
        "gpt-4o": (2.50, 10.00),
    }

    for key, (in_rate, out_rate) in rates.items():
        if key in model_id.lower():
            return (tokens_in * in_rate + tokens_out * out_rate) / 1_000_000

    # Default: Gemini Flash pricing
    return (tokens_in * 0.10 + tokens_out * 0.40) / 1_000_000
