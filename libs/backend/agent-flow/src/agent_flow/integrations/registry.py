"""Integration registry for managing external system connections."""

from __future__ import annotations

import importlib
from dataclasses import dataclass
from typing import Any, Dict, List, Optional, Type

from langchain_core.tools import BaseTool
from pydantic import BaseModel

from ..builder.models.settings import ToolSettings, ToolType


@dataclass
class IntegrationSpec:
    """Specification for an integration."""
    id: str
    name: str
    description: str
    category: str  # "database", "api", "file_system", "messaging", etc.
    tool_type: ToolType
    import_path: Optional[str] = None
    config_schema: Optional[Dict[str, Any]] = None
    required_params: List[str] = None
    optional_params: List[str] = None


class IntegrationRegistry:
    """Registry for managing available integrations."""
    
    def __init__(self):
        self._integrations: Dict[str, IntegrationSpec] = {}
        self._load_builtin_integrations()
    
    def register(self, spec: IntegrationSpec) -> None:
        """Register an integration."""
        self._integrations[spec.id] = spec
    
    def get(self, integration_id: str) -> Optional[IntegrationSpec]:
        """Get an integration by ID."""
        return self._integrations.get(integration_id)
    
    def list_by_category(self, category: str) -> List[IntegrationSpec]:
        """List integrations by category."""
        return [spec for spec in self._integrations.values() if spec.category == category]
    
    def list_all(self) -> List[IntegrationSpec]:
        """List all available integrations."""
        return list(self._integrations.values())
    
    def create_tool(self, integration_id: str, params: Dict[str, Any]) -> BaseTool:
        """Create a tool instance from an integration."""
        spec = self.get(integration_id)
        if not spec:
            raise ValueError(f"Integration '{integration_id}' not found")
        
        # Validate required parameters
        if spec.required_params:
            missing = [p for p in spec.required_params if p not in params]
            if missing:
                raise ValueError(f"Missing required parameters: {missing}")
        
        # Create tool settings
        tool_settings = ToolSettings(
            id=integration_id,
            type=spec.tool_type,
            name=spec.name,
            import_path=spec.import_path,
            params=params
        )
        
        # Use existing tool factory
        from ..builder.factories.tool_factory import ToolFactory, ToolBuildContext
        return ToolFactory.create(tool_settings, ToolBuildContext())
    
    def _load_builtin_integrations(self) -> None:
        """Load built-in integrations."""
        
        # Database integrations
        self.register(IntegrationSpec(
            id="postgresql",
            name="PostgreSQL Database",
            description="Connect to PostgreSQL databases",
            category="database",
            tool_type=ToolType.python,
            import_path="agent_flow.integrations.databases.postgresql:query",
            required_params=["connection_string"],
            optional_params=["timeout"]
        ))
        
        self.register(IntegrationSpec(
            id="mysql",
            name="MySQL Database", 
            description="Connect to MySQL databases",
            category="database",
            tool_type=ToolType.python,
            import_path="agent_flow.integrations.databases.mysql:query",
            required_params=["connection_string"],
            optional_params=["timeout"]
        ))
        
        # API integrations
        self.register(IntegrationSpec(
            id="rest_api",
            name="REST API Client",
            description="Make HTTP requests to REST APIs",
            category="api",
            tool_type=ToolType.python,
            import_path="agent_flow.integrations.apis.rest:call",
            required_params=["base_url"],
            optional_params=["headers", "auth", "timeout"]
        ))
        
        self.register(IntegrationSpec(
            id="graphql_api",
            name="GraphQL API Client",
            description="Execute GraphQL queries",
            category="api", 
            tool_type=ToolType.python,
            import_path="agent_flow.integrations.apis.graphql:query",
            required_params=["endpoint"],
            optional_params=["headers", "auth"]
        ))
        
        # File system integrations
        self.register(IntegrationSpec(
            id="local_files",
            name="Local File System",
            description="Read and write local files",
            category="file_system",
            tool_type=ToolType.python,
            import_path="agent_flow.integrations.filesystems.local:file_operations",
            required_params=["base_path"],
            optional_params=["allowed_extensions"]
        ))
        
        self.register(IntegrationSpec(
            id="google_drive",
            name="Google Drive",
            description="Access Google Drive files",
            category="file_system",
            tool_type=ToolType.python,
            import_path="agent_flow.integrations.filesystems.gdrive:drive_operations",
            required_params=["credentials"],
            optional_params=["folder_id"]
        ))
        
        # Messaging integrations
        self.register(IntegrationSpec(
            id="slack",
            name="Slack",
            description="Send messages to Slack channels",
            category="messaging",
            tool_type=ToolType.python,
            import_path="agent_flow.integrations.messaging.slack:send_message",
            required_params=["token"],
            optional_params=["default_channel"]
        ))


# Global registry instance
integration_registry = IntegrationRegistry()