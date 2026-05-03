from __future__ import annotations

import asyncio
import json
import subprocess
import threading
from typing import Any, Dict, List, Optional

from langchain_core.tools import BaseTool, StructuredTool
from pydantic import BaseModel, Field

from ..models.settings import MCPServerConfig, MCPTransport


class MCPClient:
    """Simple MCP client for stdio transport."""
    
    def __init__(self, config: MCPServerConfig):
        self.config = config
        self.process: Optional[subprocess.Popen] = None
        self._lock = threading.Lock()
        self._request_id = 0
    
    def start(self):
        """Start the MCP server process."""
        if self.config.transport != MCPTransport.stdio:
            raise NotImplementedError(f"Transport {self.config.transport} not implemented")
        
        with self._lock:
            if self.process is None:
                self.process = subprocess.Popen(
                    [self.config.command] + self.config.args,
                    stdin=subprocess.PIPE,
                    stdout=subprocess.PIPE,
                    stderr=subprocess.PIPE,
                    text=True,
                    env={**self.config.env}
                )
    
    def call_tool(self, tool_name: str, arguments: Dict[str, Any]) -> Any:
        """Call a tool on the MCP server."""
        if not self.process:
            self.start()
        
        with self._lock:
            self._request_id += 1
            request = {
                "jsonrpc": "2.0",
                "id": self._request_id,
                "method": "tools/call",
                "params": {
                    "name": tool_name,
                    "arguments": arguments
                }
            }
            
            try:
                self.process.stdin.write(json.dumps(request) + "\n")
                self.process.stdin.flush()
                
                response_line = self.process.stdout.readline()
                response = json.loads(response_line)
                
                if "error" in response:
                    raise RuntimeError(f"MCP tool error: {response['error']}")
                
                return response.get("result", {})
            except Exception as e:
                raise RuntimeError(f"MCP communication error: {e}")
    
    def stop(self):
        """Stop the MCP server process."""
        with self._lock:
            if self.process:
                self.process.terminate()
                self.process = None


class MCPToolAdapter:
    _clients: Dict[str, MCPClient] = {}
    _servers: Dict[str, MCPServerConfig] = {}
    
    @classmethod
    def register_server(cls, server_config: MCPServerConfig):
        """Register an MCP server configuration."""
        cls._servers[server_config.id] = server_config
        cls._clients[server_config.id] = MCPClient(server_config)
    
    @classmethod
    def create_tool(
        cls,
        *,
        server_id: Optional[str],
        tool_name: Optional[str],
        name: str,
        params: Dict[str, Any],
        deterministic: bool = False,
    ) -> BaseTool:
        if not server_id or not tool_name:
            raise ValueError("MCP tool requires 'mcp_server' and 'mcp_tool'")
        
        if server_id not in cls._clients:
            raise ValueError(f"MCP server '{server_id}' not registered")
        
        client = cls._clients[server_id]
        
        def _call_mcp(**kwargs: Any) -> Any:
            """Call the MCP tool with combined params and kwargs."""
            arguments = {**params, **kwargs}
            try:
                return client.call_tool(tool_name, arguments)
            except Exception as e:
                # Fallback for development/testing
                return {
                    "server": server_id,
                    "tool": tool_name,
                    "args": arguments,
                    "error": str(e),
                    "deterministic": deterministic,
                }
        
        return StructuredTool.from_function(
            name=name,
            func=_call_mcp,
            description=f"MCP tool: {tool_name} from server {server_id}",
        )
