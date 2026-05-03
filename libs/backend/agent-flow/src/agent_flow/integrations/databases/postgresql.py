"""PostgreSQL database integration."""

from typing import Any, Dict, List, Optional
import json
import time


def query(connection_string: str, sql: str, params: Optional[Dict[str, Any]] = None, timeout: int = 30) -> Dict[str, Any]:
    """Execute a SQL query against PostgreSQL database.
    
    Args:
        connection_string: PostgreSQL connection string
        sql: SQL query to execute
        params: Query parameters
        timeout: Query timeout in seconds
        
    Returns:
        Query results as dictionary
    """
    try:
        # Try to use psycopg2 if available, otherwise return mock
        try:
            import psycopg2
            import psycopg2.extras
            from psycopg2 import sql as psql
            
            with psycopg2.connect(connection_string, connect_timeout=timeout) as conn:
                with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
                    start_time = time.time()
                    
                    if params:
                        cur.execute(sql, params)
                    else:
                        cur.execute(sql)
                    
                    execution_time = (time.time() - start_time) * 1000
                    
                    if cur.description:  # SELECT query
                        rows = [dict(row) for row in cur.fetchall()]
                        return {
                            "status": "success",
                            "query": sql,
                            "params": params or {},
                            "rows": rows,
                            "row_count": len(rows),
                            "execution_time_ms": round(execution_time, 2)
                        }
                    else:  # INSERT/UPDATE/DELETE
                        return {
                            "status": "success",
                            "query": sql,
                            "params": params or {},
                            "rows_affected": cur.rowcount,
                            "execution_time_ms": round(execution_time, 2)
                        }
                        
        except ImportError:
            # psycopg2 not available, return mock response
            return {
                "status": "mock",
                "message": "psycopg2 not installed, returning mock data",
                "query": sql,
                "params": params or {},
                "rows": [
                    {"id": 1, "name": "Sample Record", "created_at": "2025-01-14T10:00:00Z"},
                    {"id": 2, "name": "Another Record", "created_at": "2025-01-14T11:00:00Z"}
                ],
                "row_count": 2,
                "execution_time_ms": 45
            }
            
    except Exception as e:
        return {
            "status": "error",
            "error": str(e),
            "query": sql,
            "params": params or {}
        }