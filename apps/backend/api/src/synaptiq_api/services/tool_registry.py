"""
Tool Registry — Catalog of available tools for AI workflow agents.

Each tool entry has:
  - id: Unique tool identifier (used in workflow specs)
  - name: Human-readable display name
  - description: What the tool does
  - category: Grouping category (e.g., 'search', 'code', 'data')
  - icon: Emoji icon for display
"""
from dataclasses import dataclass, field, asdict
from typing import Any

# ---------------------------------------------------------------------------
# Model
# ---------------------------------------------------------------------------

@dataclass(frozen=True)
class ToolDefinition:
    """A tool available for use in workflow agent nodes."""
    id: str
    name: str
    description: str
    category: str
    icon: str = "🔧"

    def to_dict(self) -> dict[str, Any]:
        return asdict(self)


# ---------------------------------------------------------------------------
# Built-in Tool Catalog
# ---------------------------------------------------------------------------

TOOL_CATALOG: list[ToolDefinition] = [
    # ── Search & Retrieval ────────────────────────────────────────────────
    ToolDefinition(
        id="web_search",
        name="Web Search",
        description="Search the web for current information, news, and articles",
        category="search",
        icon="🔍",
    ),
    ToolDefinition(
        id="document_search",
        name="Document Search",
        description="Search through uploaded documents and knowledge base",
        category="search",
        icon="📄",
    ),
    ToolDefinition(
        id="wikipedia_lookup",
        name="Wikipedia Lookup",
        description="Look up factual information from Wikipedia",
        category="search",
        icon="📚",
    ),
    ToolDefinition(
        id="arxiv_search",
        name="arXiv Search",
        description="Search academic papers on arXiv",
        category="search",
        icon="🎓",
    ),

    # ── Code & Development ────────────────────────────────────────────────
    ToolDefinition(
        id="code_interpreter",
        name="Code Interpreter",
        description="Execute Python code for calculations, data analysis, and transformations",
        category="code",
        icon="🐍",
    ),
    ToolDefinition(
        id="code_review",
        name="Code Review",
        description="Analyze code for bugs, best practices, and security issues",
        category="code",
        icon="🔎",
    ),
    ToolDefinition(
        id="sql_executor",
        name="SQL Executor",
        description="Execute SQL queries against connected databases",
        category="code",
        icon="🗄️",
    ),
    ToolDefinition(
        id="api_caller",
        name="API Caller",
        description="Make HTTP requests to external APIs",
        category="code",
        icon="🌐",
    ),

    # ── Data Processing ───────────────────────────────────────────────────
    ToolDefinition(
        id="csv_parser",
        name="CSV Parser",
        description="Parse and analyze CSV/tabular data files",
        category="data",
        icon="📊",
    ),
    ToolDefinition(
        id="json_transformer",
        name="JSON Transformer",
        description="Transform and restructure JSON data",
        category="data",
        icon="🔄",
    ),
    ToolDefinition(
        id="data_validator",
        name="Data Validator",
        description="Validate data against schemas or business rules",
        category="data",
        icon="✅",
    ),
    ToolDefinition(
        id="text_extractor",
        name="Text Extractor",
        description="Extract text from PDFs, images, and documents (OCR)",
        category="data",
        icon="📝",
    ),

    # ── Communication ─────────────────────────────────────────────────────
    ToolDefinition(
        id="email_sender",
        name="Email Sender",
        description="Send emails with customizable templates",
        category="communication",
        icon="📧",
    ),
    ToolDefinition(
        id="slack_notifier",
        name="Slack Notifier",
        description="Send notifications to Slack channels",
        category="communication",
        icon="💬",
    ),
    ToolDefinition(
        id="webhook_trigger",
        name="Webhook Trigger",
        description="Trigger external webhooks with custom payloads",
        category="communication",
        icon="🔔",
    ),

    # ── AI & ML ───────────────────────────────────────────────────────────
    ToolDefinition(
        id="summarizer",
        name="Text Summarizer",
        description="Summarize long texts into concise versions",
        category="ai",
        icon="📋",
    ),
    ToolDefinition(
        id="translator",
        name="Translator",
        description="Translate text between languages",
        category="ai",
        icon="🌍",
    ),
    ToolDefinition(
        id="sentiment_analyzer",
        name="Sentiment Analyzer",
        description="Analyze sentiment and emotion in text",
        category="ai",
        icon="😊",
    ),
    ToolDefinition(
        id="image_generator",
        name="Image Generator",
        description="Generate images from text descriptions",
        category="ai",
        icon="🎨",
    ),

    # ── Utilities ─────────────────────────────────────────────────────────
    ToolDefinition(
        id="calculator",
        name="Calculator",
        description="Perform mathematical calculations and conversions",
        category="utility",
        icon="🧮",
    ),
    ToolDefinition(
        id="date_time",
        name="Date/Time",
        description="Date parsing, formatting, timezone conversions",
        category="utility",
        icon="📅",
    ),
    ToolDefinition(
        id="file_manager",
        name="File Manager",
        description="Read, write, and manage files in the workspace",
        category="utility",
        icon="📁",
    ),
    ToolDefinition(
        id="human_in_the_loop",
        name="Human in the Loop",
        description="Pause execution to request human input or approval",
        category="utility",
        icon="🙋",
    ),
]

# Lookup for quick validation
_TOOL_INDEX: dict[str, ToolDefinition] = {t.id: t for t in TOOL_CATALOG}

# Category metadata for grouping in UI
TOOL_CATEGORIES: dict[str, dict[str, str]] = {
    "search": {"label": "Search & Retrieval", "icon": "🔍"},
    "code": {"label": "Code & Development", "icon": "💻"},
    "data": {"label": "Data Processing", "icon": "📊"},
    "communication": {"label": "Communication", "icon": "📧"},
    "ai": {"label": "AI & ML", "icon": "🤖"},
    "utility": {"label": "Utilities", "icon": "🛠️"},
}


def get_tool(tool_id: str) -> ToolDefinition | None:
    """Get a tool definition by ID."""
    return _TOOL_INDEX.get(tool_id)


def get_all_tools() -> list[dict[str, Any]]:
    """Return all tools as serializable dicts."""
    return [t.to_dict() for t in TOOL_CATALOG]


def get_tools_by_category() -> dict[str, list[dict[str, Any]]]:
    """Return tools grouped by category."""
    grouped: dict[str, list[dict[str, Any]]] = {}
    for tool in TOOL_CATALOG:
        grouped.setdefault(tool.category, []).append(tool.to_dict())
    return grouped


def validate_tools(tool_ids: list[str]) -> tuple[list[str], list[str]]:
    """Validate a list of tool IDs. Returns (valid, invalid)."""
    valid = [tid for tid in tool_ids if tid in _TOOL_INDEX]
    invalid = [tid for tid in tool_ids if tid not in _TOOL_INDEX]
    return valid, invalid
