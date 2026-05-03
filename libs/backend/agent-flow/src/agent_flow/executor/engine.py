from __future__ import annotations

import threading
import time
import uuid
from dataclasses import dataclass, field
from typing import Any, Dict, Generator, Iterable, Mapping, Optional

from langchain_core.messages import HumanMessage
from langchain_core.runnables import RunnableConfig

from agent_flow.builder.models.settings import FlowSettings
from ..builder import build_workflow


@dataclass
class _RunState:
    spec: FlowSettings
    graph: Any
    created_at: float = field(default_factory=lambda: time.time())
    cancel_evt: threading.Event = field(default_factory=threading.Event)
    done: bool = False
    result: Any | None = None
    error: Exception | None = None




class FlowExecutor:
    """Executor API for starting, streaming, canceling, and fetching results."""

    def __init__(self) -> None:
        self._runs: Dict[str, _RunState] = {}
        self._lock = threading.Lock()

    def start_run(
        self,
        *,
        spec_json: str | Mapping[str, Any],
        input: str | Mapping[str, Any] | Iterable[Mapping[str, Any]] | None = None,
        config: Optional[RunnableConfig] = None,
    ) -> str:
        """Start a run.

        input: messages or initial state for the graph. If a string, it's turned into a HumanMessage.
        Returns run_id.
        """
        graph, spec, _ctx = build_workflow(spec_json)

        run = _RunState(spec=spec, graph=graph)
        run_id = str(uuid.uuid4())
        with self._lock:
            self._runs[run_id] = run

        # Store initial input in the run object for later use during streaming
        setattr(run, "_input", input)
        setattr(run, "_config", config or RunnableConfig())
        return run_id

    def stream_run(self, run_id: str) -> Generator[Dict[str, Any], None, None]:
        run = self._get_run(run_id)
        input_state = self._to_initial_state(getattr(run, "_input", None))
        config: RunnableConfig = getattr(run, "_config", RunnableConfig())

        # Overall timeout support
        total_timeout_ms = run.spec.policy.resources.total_timeout_ms
        deadline = time.time() + (total_timeout_ms / 1000.0) if total_timeout_ms else None

        def _check_cancel_or_timeout() -> None:
            if run.cancel_evt.is_set():
                raise RuntimeError("Run cancelled")
            if deadline and time.time() > deadline:
                raise TimeoutError("Run exceeded total timeout")

        try:
            # Prefer streaming if available
            if hasattr(run.graph, "stream"):
                for _, event in run.graph.stream(
                    input_state, config=config
                ):
                    _check_cancel_or_timeout()
                    # event contains state updates at each node
                    yield {
                        "type": "step",
                        "event": _safe_dump(event),
                        "ts": time.time(),
                    }
                # Final result after stream finishes
                out = run.graph.invoke(input_state, config=config)
            else:
                out = run.graph.invoke(input_state, config=config)

            run.result = _safe_dump(out)
            run.done = True
            yield {"type": "result", "result": run.result, "ts": time.time()}
        except Exception as e:  # capture and set error
            run.error = e
            run.done = True
            yield {"type": "error", "error": repr(e), "ts": time.time()}

    def cancel_run(self, run_id: str) -> None:
        run = self._get_run(run_id)
        run.cancel_evt.set()

    def get_result(self, run_id: str) -> Dict[str, Any] | None:
        run = self._get_run(run_id)
        if run.result is not None:
            return run.result
        if run.error is not None:
            raise run.error
        return None

    # Helpers
    def _get_run(self, run_id: str) -> _RunState:
        with self._lock:
            if run_id not in self._runs:
                raise KeyError(f"Unknown run_id: {run_id}")
            return self._runs[run_id]

    @staticmethod
    def _to_initial_state(input: Any) -> Dict[str, Any]:
        # We use MessagesState; accept various inputs for convenience
        if input is None:
            return {"messages": []}
        if isinstance(input, str):
            return {"messages": [HumanMessage(content=input)]}
        if isinstance(input, Mapping):
            # If user passes full state
            if "messages" in input:
                return dict(input)
            # Otherwise treat as a single human message content
            return {"messages": [HumanMessage(content=str(input))]}
        if isinstance(input, Iterable):
            # Assume iterable of message dicts
            return {"messages": list(input)}
        # Fallback to string conversion
        return {"messages": [HumanMessage(content=str(input))]}


def _safe_dump(obj: Any) -> Any:
    try:
        # Many langchain objects are pydantic serializable via .dict()
        if hasattr(obj, "dict"):
            return obj.dict()
        return obj
    except Exception:
        return str(obj)
