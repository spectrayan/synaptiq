import json
import logging
from typing import Dict, List, Set, Any
from fastapi import WebSocket

logger = logging.getLogger(__name__)

class ConnectionManager:
    def __init__(self):
        # Maps workflow_id -> list of active websockets
        self.active_connections: Dict[str, List[WebSocket]] = {}
        # Maps websocket -> user_info (dict)
        self.user_info: Dict[WebSocket, dict] = {}

    async def connect(self, websocket: WebSocket, workflow_id: str, user_info: dict):
        await websocket.accept()
        if workflow_id not in self.active_connections:
            self.active_connections[workflow_id] = []
        self.active_connections[workflow_id].append(websocket)
        self.user_info[websocket] = user_info
        logger.info(f"WebSocket connected for workflow {workflow_id}: {user_info.get('name')}")
        
        # Broadcast user joined
        await self.broadcast(workflow_id, {
            "type": "user_joined",
            "user": user_info
        }, exclude=websocket)
        
        # Send current users
        users = [self.user_info[ws] for ws in self.active_connections[workflow_id] if ws != websocket]
        await websocket.send_json({
            "type": "active_users",
            "users": users
        })

    def disconnect(self, websocket: WebSocket, workflow_id: str):
        if workflow_id in self.active_connections:
            if websocket in self.active_connections[workflow_id]:
                self.active_connections[workflow_id].remove(websocket)
                if not self.active_connections[workflow_id]:
                    del self.active_connections[workflow_id]
        
        user_info = self.user_info.pop(websocket, {})
        logger.info(f"WebSocket disconnected for workflow {workflow_id}: {user_info.get('name')}")
        return user_info

    async def broadcast(self, workflow_id: str, message: dict, exclude: WebSocket = None):
        if workflow_id in self.active_connections:
            for connection in self.active_connections[workflow_id]:
                if connection != exclude:
                    try:
                        await connection.send_json(message)
                    except Exception as e:
                        logger.error(f"Error broadcasting to {workflow_id}: {e}")

manager = ConnectionManager()
