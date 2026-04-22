"""StreamBuffer — suppresses DSL component JSON from SSE token stream.

During LLM streaming, this buffer intercepts and suppresses:
  1. ```component ... ```  — instructed fenced format
  2. ```json ... ```       — common LLM fallback format
  3. Bare JSON blocks      — unfenced { "type": "..." ... } objects

The suppressed blocks are NOT emitted as text tokens. Instead, the backend's
post-stream `_extract_components()` function parses the full response and emits
proper `component` SSE events.
"""
import re


# DSL component types that the frontend can render
_COMPONENT_TYPES = {
    "item_card", "item_grid", "item_detail", "comparison_table",
    "filter_summary", "result_count", "empty_state", "action_confirm",
    "info_banner", "data_table", "form_input",
    "kpi_card", "chart", "stat_grid", "metric_table",
    "kanban", "timeline", "progress_tracker",
    "launchpad", "view", "dashboard", "tabs",
}

# Quick regex check for bare JSON containing a known component type
_BARE_JSON_START = re.compile(
    r'\{\s*"type"\s*:\s*"(' + "|".join(_COMPONENT_TYPES) + r')"'
)


class StreamBuffer:
    """Buffer that suppresses DSL component blocks during SSE streaming."""

    def __init__(self):
        self.buffer = ""
        self.is_suppressing = False
        self.suppress_mode = ""  # "fence" or "bare"
        self.bare_depth = 0  # brace nesting depth for bare JSON

    def process_chunk(self, chunk: str) -> str:
        self.buffer += chunk
        to_yield = ""

        if not self.is_suppressing:
            # --- Check for fenced blocks (```component or ```json) ---
            idx_comp = self.buffer.find("```component")
            idx_json = self.buffer.find("```json")

            # Find the earliest occurrence
            idx = -1
            if idx_comp != -1 and idx_json != -1:
                idx = min(idx_comp, idx_json)
            elif idx_comp != -1:
                idx = idx_comp
            elif idx_json != -1:
                idx = idx_json

            if idx != -1:
                to_yield = self.buffer[:idx]
                self.buffer = self.buffer[idx:]  # keep from ``` onwards
                self.is_suppressing = True
                self.suppress_mode = "fence"
            elif "```" in self.buffer:
                # Might become ```component — wait
                pass
            else:
                # --- Check for bare JSON component blocks ---
                match = _BARE_JSON_START.search(self.buffer)
                if match:
                    json_start = self.buffer.rfind("{", 0, match.start() + 1)
                    if json_start >= 0:
                        to_yield = self.buffer[:json_start]
                        self.buffer = self.buffer[json_start:]
                        self.is_suppressing = True
                        self.suppress_mode = "bare"
                        self.bare_depth = 0
                elif len(self.buffer) > 30:
                    # Safe to yield everything except a trailing window
                    # (keep enough to detect `{"type":` at chunk boundaries)
                    to_yield = self.buffer[:-30]
                    self.buffer = self.buffer[-30:]

        if self.is_suppressing:
            if self.suppress_mode == "fence":
                # Look for the closing ``` after the opening fence
                next_fence = self.buffer.find("```", 3)
                if next_fence != -1:
                    # Fenced block closed
                    self.buffer = self.buffer[next_fence + 3:]
                    self.is_suppressing = False
                    self.suppress_mode = ""
                    # Process remaining buffer through normal logic
                    remaining = self.buffer
                    self.buffer = ""
                    to_yield += self.process_chunk(remaining)

            elif self.suppress_mode == "bare":
                # Count braces to find end of JSON object
                i = 0
                in_string = False
                escape = False
                while i < len(self.buffer):
                    c = self.buffer[i]
                    if escape:
                        escape = False
                    elif c == '\\' and in_string:
                        escape = True
                    elif c == '"' and not escape:
                        in_string = not in_string
                    elif not in_string:
                        if c == '{':
                            self.bare_depth += 1
                        elif c == '}':
                            self.bare_depth -= 1
                            if self.bare_depth == 0:
                                # JSON object closed
                                self.buffer = self.buffer[i + 1:]
                                self.is_suppressing = False
                                self.suppress_mode = ""
                                self.bare_depth = 0
                                # Process remaining
                                remaining = self.buffer
                                self.buffer = ""
                                to_yield += self.process_chunk(remaining)
                                break
                    i += 1

        return to_yield

    def flush(self) -> str:
        """Flush remaining buffer at end of stream."""
        if not self.is_suppressing:
            to_yield = self.buffer
            self.buffer = ""
            return to_yield
        # If we're still suppressing (incomplete block), don't yield
        return ""
