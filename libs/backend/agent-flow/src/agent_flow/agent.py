# Copyright 2025 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# mypy: disable-error-code="union-attr"
from __future__ import annotations

from typing import Any, Dict, Generator, Iterable, Mapping, Optional

from langchain_core.runnables import RunnableConfig

from .builder import build_workflow
from .executor.engine import FlowExecutor
from .utils.io import read_text

# Path to the Flow Settings JSON that defines the current workflow
_FLOW_SPEC_PATH = __package__.replace('.', '\\') + "\\flows\\simple_agent.json"  # best-effort relative path


# Expose `agent` compatible object
# Note: We compute a best-effort path to the Flow Settings located at agent_flow/flows/simple_agent.json
# If import/package resolution differs, callers can construct ExecutorAgent with an explicit path.
spec_json = read_text(_FLOW_SPEC_PATH)
agent, flow_settings, agent_context = build_workflow(spec_json)
