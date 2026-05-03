"""REST API integration."""

from typing import Any, Dict, Optional
import json


def call(
    base_url: str, 
    endpoint: str, 
    method: str = "GET", 
    headers: Optional[Dict[str, str]] = None,
    data: Optional[Dict[str, Any]] = None,
    params: Optional[Dict[str, Any]] = None,
    auth: Optional[Dict[str, str]] = None,
    timeout: int = 30
) -> Dict[str, Any]:
    """Make HTTP request to REST API.
    
    Args:
        base_url: Base URL of the API
        endpoint: API endpoint path
        method: HTTP method (GET, POST, PUT, DELETE)
        headers: Request headers
        data: Request body data
        params: Query parameters
        auth: Authentication info (dict with 'type' and credentials)
        timeout: Request timeout in seconds
        
    Returns:
        API response as dictionary
    """
    try:
        # Try to use httpx if available, otherwise return mock
        try:
            import httpx
            import time
            
            full_url = f"{base_url.rstrip('/')}/{endpoint.lstrip('/')}"
            
            # Prepare request
            request_headers = headers or {}
            request_auth = None
            
            # Handle authentication
            if auth:
                auth_type = auth.get("type", "").lower()
                if auth_type == "bearer":
                    request_headers["Authorization"] = f"Bearer {auth.get('token')}"
                elif auth_type == "basic":
                    request_auth = (auth.get("username"), auth.get("password"))
                elif auth_type == "api_key":
                    key_name = auth.get("key_name", "X-API-Key")
                    request_headers[key_name] = auth.get("api_key")
            
            start_time = time.time()
            
            with httpx.Client(timeout=timeout) as client:
                response = client.request(
                    method=method.upper(),
                    url=full_url,
                    headers=request_headers,
                    json=data if data else None,
                    params=params,
                    auth=request_auth
                )
                
                response_time = (time.time() - start_time) * 1000
                
                # Try to parse JSON response
                try:
                    response_data = response.json()
                except:
                    response_data = response.text
                
                return {
                    "status": "success",
                    "status_code": response.status_code,
                    "url": full_url,
                    "method": method.upper(),
                    "headers": dict(response.headers),
                    "response": response_data,
                    "response_time_ms": round(response_time, 2)
                }
                
        except ImportError:
            # httpx not available, return mock response
            full_url = f"{base_url.rstrip('/')}/{endpoint.lstrip('/')}"
            
            return {
                "status": "mock",
                "message": "httpx not installed, returning mock data",
                "status_code": 200,
                "url": full_url,
                "method": method,
                "headers": headers or {},
                "data": data,
                "params": params,
                "response": {
                    "message": "Mock API response",
                    "timestamp": "2025-01-14T10:00:00Z",
                    "data": {"result": "success"}
                },
                "response_time_ms": 150
            }
            
    except Exception as e:
        return {
            "status": "error",
            "error": str(e),
            "url": f"{base_url.rstrip('/')}/{endpoint.lstrip('/')}",
            "method": method
        }