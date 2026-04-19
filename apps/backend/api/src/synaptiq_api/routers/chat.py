"""Chat router — streaming LLM responses via SSE."""
from fastapi import APIRouter, Depends, Request
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

router = APIRouter()


class ChatRequest(BaseModel):
    session_id: str
    message: str
    model_override: str | None = None


class ChatResponse(BaseModel):
    session_id: str
    turn_id: str
    content: str
    ui_components: list[dict] | None = None
    actions: list[dict] | None = None


@router.post("/", summary="Send a chat message (non-streaming)")
async def chat(body: ChatRequest, request: Request) -> ChatResponse:
    tenant_id: str = request.state.tenant_id  # noqa: F841
    # TODO: wire up LLM orchestration service
    return ChatResponse(
        session_id=body.session_id,
        turn_id="placeholder",
        content="Echo: " + body.message,
    )


@router.post("/stream", summary="Send a chat message (SSE streaming)")
async def chat_stream(body: ChatRequest, request: Request) -> StreamingResponse:
    tenant_id: str = request.state.tenant_id  # noqa: F841

    async def event_generator():
        # TODO: yield actual LLM token stream
        yield f"data: {body.message}\n\n"
        yield "data: [DONE]\n\n"

    return StreamingResponse(event_generator(), media_type="text/event-stream")


@router.get("/sessions/{session_id}/history", summary="Get conversation history")
async def get_history(session_id: str, request: Request) -> dict:
    tenant_id: str = request.state.tenant_id  # noqa: F841
    return {"session_id": session_id, "turns": []}
