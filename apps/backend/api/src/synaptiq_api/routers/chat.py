"""Chat router — SSE streaming LLM responses + session management (T6.8, T6.12–T6.14)."""
import uuid
from datetime import datetime

from fastapi import APIRouter, HTTPException, Request, status
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field

from synaptiq_api.core.mongodb import get_db
from synaptiq_api.services.chat_service import chat_stream

router = APIRouter()


# ---------------------------------------------------------------------------
# Request / Response models
# ---------------------------------------------------------------------------

class ChatMessageRequest(BaseModel):
    """POST /chat/message — send a chat message."""
    session_id: str = Field(..., description="Client-generated session UUID")
    message: str = Field(..., min_length=1, max_length=4000)
    model_override: str | None = None


class SessionCreateRequest(BaseModel):
    """POST /chat/sessions — create a new session."""
    session_id: str = Field(
        default_factory=lambda: str(uuid.uuid4()),
        description="Client-generated UUID (optional — generated if omitted)",
    )
    metadata: dict | None = None


class SessionResponse(BaseModel):
    session_id: str
    tenant_id: str
    created_at: str
    turn_count: int = 0


class HistoryResponse(BaseModel):
    session_id: str
    turns: list[dict]
    total: int


# ---------------------------------------------------------------------------
# T6.8 — SSE Chat endpoint
# ---------------------------------------------------------------------------

@router.post(
    "/message",
    summary="Send a chat message (SSE stream)",
    response_class=StreamingResponse,
)
async def post_chat_message(body: ChatMessageRequest, request: Request):
    """
    Stream an AI response via Server-Sent Events (REQ-C7, REQ-NF2).

    Events emitted:
      - `token`: individual text tokens
      - `component`: parsed DSL component JSON
      - `status`: progress/fallback status messages
      - `done`: final metadata (turn_id, token counts)
      - `error`: error messages
    """
    tenant_id: str = request.state.tenant_id

    return StreamingResponse(
        chat_stream(
            tenant_id=tenant_id,
            session_id=body.session_id,
            user_message=body.message,
            model_override=body.model_override,
        ),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",  # Disable nginx buffering
        },
    )


# ---------------------------------------------------------------------------
# T6.12 — Create session
# ---------------------------------------------------------------------------

@router.post("/sessions", summary="Create a new chat session", status_code=201)
async def create_session(
    body: SessionCreateRequest,
    request: Request,
) -> SessionResponse:
    """
    Create a new anonymous chat session (REQ-C3, REQ-NF-SL1).

    The session_id is client-generated (UUID). If omitted, one is generated.
    Sessions are scoped to the tenant and expire per tenant TTL config.
    """
    tenant_id: str = request.state.tenant_id
    db = get_db()

    # Check for duplicate
    existing = await db["sessions"].find_one({
        "session_id": body.session_id,
        "tenant_id": tenant_id,
    })
    if existing:
        return SessionResponse(
            session_id=existing["session_id"],
            tenant_id=tenant_id,
            created_at=existing["created_at"].isoformat(),
            turn_count=len(existing.get("turns", [])),
        )

    now = datetime.utcnow()
    session = {
        "session_id": body.session_id,
        "tenant_id": tenant_id,
        "turns": [],
        "active_filters": [],
        "metadata": body.metadata or {},
        "created_at": now,
        "updated_at": now,
    }
    await db["sessions"].insert_one(session)

    return SessionResponse(
        session_id=body.session_id,
        tenant_id=tenant_id,
        created_at=now.isoformat(),
    )


# ---------------------------------------------------------------------------
# T6.13 — Get session history
# ---------------------------------------------------------------------------

@router.get(
    "/sessions/{session_id}/history",
    summary="Get conversation history",
)
async def get_session_history(
    session_id: str,
    request: Request,
    limit: int = 50,
    offset: int = 0,
) -> HistoryResponse:
    """Retrieve conversation turns for a session."""
    tenant_id: str = request.state.tenant_id
    db = get_db()

    session = await db["sessions"].find_one({
        "session_id": session_id,
        "tenant_id": tenant_id,
    })
    if not session:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Session {session_id} not found",
        )

    turns = session.get("turns", [])
    total = len(turns)

    # Apply pagination
    paginated = turns[offset:offset + limit]

    return HistoryResponse(
        session_id=session_id,
        turns=paginated,
        total=total,
    )


# ---------------------------------------------------------------------------
# T6.14 — Delete / reset session
# ---------------------------------------------------------------------------

@router.delete(
    "/sessions/{session_id}",
    summary="Clear/reset conversation session",
    status_code=204,
)
async def delete_session(session_id: str, request: Request):
    """
    Clear session history and reset the conversation (REQ-C9).

    Does not delete the session document — just clears turns and filters.
    """
    tenant_id: str = request.state.tenant_id
    db = get_db()

    result = await db["sessions"].update_one(
        {"session_id": session_id, "tenant_id": tenant_id},
        {
            "$set": {
                "turns": [],
                "active_filters": [],
                "updated_at": datetime.utcnow(),
            },
        },
    )

    if result.matched_count == 0:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Session {session_id} not found",
        )
