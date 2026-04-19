"""Chat router — SSE streaming LLM responses + session management (T6.8, T6.12–T6.14)."""
import uuid
from datetime import datetime

from fastapi import APIRouter, HTTPException, Request, status
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field

from synaptiq_api.core.mongodb import get_db
from synaptiq_api.services.chat_service import chat_stream
from pymongo import ReturnDocument

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
    title: str = "New conversation"
    updated_at: str | None = None


class SessionListItem(BaseModel):
    session_id: str
    title: str
    turn_count: int
    created_at: str
    updated_at: str


class SessionListResponse(BaseModel):
    sessions: list[SessionListItem]
    total: int


class HistoryResponse(BaseModel):
    session_id: str
    turns: list[dict]
    total: int


class SessionUpdateRequest(BaseModel):
    title: str | None = None


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

    # Extract user role from Firebase custom claims (set by auth middleware)
    user_info = getattr(request.state, "user", {}) or {}
    user_role: str = user_info.get("role") or "user"

    return StreamingResponse(
        chat_stream(
            tenant_id=tenant_id,
            session_id=body.session_id,
            user_message=body.message,
            model_override=body.model_override,
            user_role=user_role,
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
# T6.12b — List sessions
# ---------------------------------------------------------------------------

@router.get("/sessions", summary="List all chat sessions")
async def list_sessions(
    request: Request,
    limit: int = 20,
    offset: int = 0,
) -> SessionListResponse:
    """
    List all sessions for the current tenant, ordered by most recent activity.
    Auto-generates a title from the first user message if not set.
    """
    tenant_id: str = request.state.tenant_id
    db = get_db()

    total = await db["sessions"].count_documents({"tenant_id": tenant_id})

    cursor = db["sessions"].find(
        {"tenant_id": tenant_id},
        {"session_id": 1, "title": 1, "turns": 1, "created_at": 1, "updated_at": 1},
    ).sort("updated_at", -1).skip(offset).limit(limit)

    sessions: list[SessionListItem] = []
    async for doc in cursor:
        # Auto-generate title from first user message if no explicit title
        title = doc.get("title", "")
        if not title:
            turns = doc.get("turns", [])
            for turn in turns:
                if turn.get("role") == "user":
                    title = turn.get("content", "New conversation")[:50]
                    break
            if not title:
                title = "New conversation"

        sessions.append(SessionListItem(
            session_id=doc["session_id"],
            title=title,
            turn_count=len(doc.get("turns", [])),
            created_at=doc["created_at"].isoformat(),
            updated_at=doc.get("updated_at", doc["created_at"]).isoformat(),
        ))

    return SessionListResponse(sessions=sessions, total=total)


# ---------------------------------------------------------------------------
# T6.12c — Update session (title)
# ---------------------------------------------------------------------------

@router.patch(
    "/sessions/{session_id}",
    summary="Update session metadata (e.g. title)",
)
async def update_session(
    session_id: str,
    body: SessionUpdateRequest,
    request: Request,
) -> SessionResponse:
    """Update mutable session fields (title)."""
    tenant_id: str = request.state.tenant_id
    db = get_db()

    update: dict = {"updated_at": datetime.utcnow()}
    if body.title is not None:
        update["title"] = body.title

    result = await db["sessions"].find_one_and_update(
        {"session_id": session_id, "tenant_id": tenant_id},
        {"$set": update},
        return_document=True,
    )
    if not result:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Session {session_id} not found",
        )

    return SessionResponse(
        session_id=result["session_id"],
        tenant_id=tenant_id,
        created_at=result["created_at"].isoformat(),
        turn_count=len(result.get("turns", [])),
        title=result.get("title", "New conversation"),
        updated_at=result["updated_at"].isoformat(),
    )


# ---------------------------------------------------------------------------
# T6.12d — Delete session
# ---------------------------------------------------------------------------

@router.delete(
    "/sessions/{session_id}",
    summary="Delete a chat session",
    status_code=status.HTTP_204_NO_CONTENT,
)
async def delete_session(
    session_id: str,
    request: Request,
):
    """Permanently delete a session and all its turns."""
    tenant_id: str = request.state.tenant_id
    db = get_db()

    result = await db["sessions"].delete_one(
        {"session_id": session_id, "tenant_id": tenant_id}
    )
    if result.deleted_count == 0:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Session {session_id} not found",
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
