from __future__ import annotations

import json
from pathlib import Path
from typing import Any


def read_text(path: str | Path, encoding: str = "utf-8") -> str:
    """Read and return file contents as text.

    Args:
        path: File system path to read.
        encoding: Text encoding to use.
    """
    p = Path(path)
    return p.read_text(encoding=encoding)


def read_json(path: str | Path, encoding: str = "utf-8") -> Any:
    """Read JSON file and return parsed object."""
    txt = read_text(path, encoding=encoding)
    return json.loads(txt)
