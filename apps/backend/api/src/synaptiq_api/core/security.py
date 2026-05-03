"""Security utilities — password hashing and JWT operations for built-in auth."""
import logging
from datetime import datetime, timedelta, timezone
from uuid import uuid4

import bcrypt
from jose import JWTError, jwt

from synaptiq_api.core.config import settings

logger = logging.getLogger(__name__)

# ── Password Hashing ─────────────────────────────────────────────────────────


def hash_password(plain: str) -> str:
    """Hash a plaintext password with bcrypt."""
    return bcrypt.hashpw(plain.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")


def verify_password(plain: str, hashed: str) -> bool:
    """Verify a plaintext password against a bcrypt hash."""
    return bcrypt.checkpw(plain.encode("utf-8"), hashed.encode("utf-8"))


# ── JWT Tokens ────────────────────────────────────────────────────────────────

ALGORITHM = "HS256"


def create_access_token(
    user_id: str,
    email: str,
    role: str = "",
    tenant_id: str = "",
    must_change_password: bool = False,
) -> tuple[str, int]:
    """
    Create a signed JWT access token.

    Returns:
        Tuple of (token_string, expires_in_seconds).
    """
    expires_delta = timedelta(hours=settings.jwt_expiry_hours)
    expire = datetime.now(timezone.utc) + expires_delta
    expires_in = int(expires_delta.total_seconds())

    payload = {
        "sub": user_id,
        "email": email,
        "role": role,
        "tenant_id": tenant_id,
        "must_change_password": must_change_password,
        "iat": datetime.now(timezone.utc),
        "exp": expire,
    }
    token = jwt.encode(payload, settings.jwt_secret, algorithm=ALGORITHM)
    return token, expires_in


def create_refresh_token() -> str:
    """Create a random refresh token (UUID)."""
    return str(uuid4())


def decode_access_token(token: str) -> dict:
    """
    Decode and verify a JWT access token.

    Returns:
        Decoded claims dict.

    Raises:
        JWTError: If token is invalid or expired.
    """
    try:
        payload = jwt.decode(token, settings.jwt_secret, algorithms=[ALGORITHM])
        return payload
    except JWTError as e:
        logger.warning("JWT decode failed: %s", e)
        raise
