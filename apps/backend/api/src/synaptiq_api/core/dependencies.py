"""Auth dependencies — reusable FastAPI dependencies for role-based access."""
from fastapi import HTTPException, Request, status


def require_auth(request: Request) -> dict:
    """
    Dependency that ensures the request has a valid authenticated user.
    Extracts user claims from request.state (set by AuthMiddleware).
    """
    user = getattr(request.state, "user", None)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authentication required",
        )
    return user


def require_role(*allowed_roles: str):
    """
    Dependency factory — ensures the authenticated user has one of the allowed roles.

    Usage:
        @router.post("/", dependencies=[Depends(require_role("platform_admin"))])
    """
    def _check(request: Request) -> dict:
        user = require_auth(request)
        role = user.get("role") or (user.get("custom_claims") or {}).get("role")
        if role not in allowed_roles:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"Insufficient permissions. Required: {', '.join(allowed_roles)}",
            )
        return user
    return _check


def require_platform_admin(request: Request) -> dict:
    """Dependency — requires platform_admin role."""
    return require_role("platform_admin")(request)


def require_tenant_admin(request: Request) -> dict:
    """Dependency — requires platform_admin or tenant_admin role."""
    return require_role("platform_admin", "tenant_admin")(request)
