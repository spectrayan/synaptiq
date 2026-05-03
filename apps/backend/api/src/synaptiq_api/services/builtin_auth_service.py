"""Built-in auth service — MongoDB users collection + JWT tokens.

Used when AUTH_PROVIDER=builtin. Provides signup, login, password change,
and user management without any external auth dependency.
"""
import logging
from datetime import datetime, timezone

from bson import ObjectId
from motor.motor_asyncio import AsyncIOMotorDatabase

from synaptiq_api.core.mongodb import get_db
from synaptiq_api.core.security import (
    create_access_token,
    create_refresh_token,
    hash_password,
    verify_password,
)
from synaptiq_api.models.auth import (
    LoginRequest,
    SignUpRequest,
    UserResponse,
)
from synaptiq_api.models.user import BuiltinTokenResponse, ChangePasswordRequest

logger = logging.getLogger(__name__)

USERS_COLLECTION = "users"


class BuiltinAuthService:
    """Service for built-in JWT authentication backed by MongoDB."""

    # ── Signup ────────────────────────────────────────────────────────────────

    @staticmethod
    async def signup(request: SignUpRequest) -> UserResponse:
        """Create a new user with email/password in MongoDB."""
        db: AsyncIOMotorDatabase = get_db()
        collection = db[USERS_COLLECTION]

        # Check for existing user
        existing = await collection.find_one({"email": request.email})
        if existing:
            raise ValueError("Email already registered")

        now = datetime.now(timezone.utc)
        doc = {
            "email": request.email,
            "password_hash": hash_password(request.password),
            "display_name": "",
            "role": "tenant_viewer",
            "tenant_id": "",
            "must_change_password": False,
            "email_verified": False,
            "disabled": False,
            "created_at": now,
            "updated_at": now,
        }
        result = await collection.insert_one(doc)
        logger.info("Created builtin user: %s (id=%s)", request.email, result.inserted_id)

        return UserResponse(
            uid=str(result.inserted_id),
            email=request.email,
            email_verified=False,
            display_name=None,
        )

    # ── Login ─────────────────────────────────────────────────────────────────

    @staticmethod
    async def login(request: LoginRequest) -> BuiltinTokenResponse:
        """
        Authenticate with email/password and return a JWT token.

        Raises:
            ValueError: Invalid credentials or account disabled.
        """
        db: AsyncIOMotorDatabase = get_db()
        collection = db[USERS_COLLECTION]

        user = await collection.find_one({"email": request.email})
        if not user:
            raise ValueError("Invalid email or password")

        if user.get("disabled"):
            raise ValueError("Account is disabled")

        if not verify_password(request.password, user["password_hash"]):
            raise ValueError("Invalid email or password")

        user_id = str(user["_id"])
        must_change = user.get("must_change_password", False)

        token, expires_in = create_access_token(
            user_id=user_id,
            email=user["email"],
            role=user.get("role", ""),
            tenant_id=user.get("tenant_id", ""),
            must_change_password=must_change,
        )
        refresh = create_refresh_token()

        return BuiltinTokenResponse(
            id_token=token,
            refresh_token=refresh,
            expires_in=expires_in,
            must_change_password=must_change,
            user={
                "uid": user_id,
                "email": user["email"],
                "display_name": user.get("display_name", ""),
                "role": user.get("role", ""),
                "tenant_id": user.get("tenant_id", ""),
            },
        )

    # ── Get User ──────────────────────────────────────────────────────────────

    @staticmethod
    async def get_user_by_uid(uid: str) -> UserResponse:
        """Get user by MongoDB _id."""
        db: AsyncIOMotorDatabase = get_db()
        collection = db[USERS_COLLECTION]

        user = await collection.find_one({"_id": ObjectId(uid)})
        if not user:
            raise ValueError("User not found")

        return UserResponse(
            uid=str(user["_id"]),
            email=user["email"],
            email_verified=user.get("email_verified", False),
            display_name=user.get("display_name"),
            custom_claims={
                "role": user.get("role", ""),
                "tenant_id": user.get("tenant_id", ""),
            },
        )

    # ── Change Password ───────────────────────────────────────────────────────

    @staticmethod
    async def change_password(uid: str, request: ChangePasswordRequest) -> dict:
        """
        Change a user's password.

        Verifies current password, hashes new one, clears must_change_password.
        """
        db: AsyncIOMotorDatabase = get_db()
        collection = db[USERS_COLLECTION]

        user = await collection.find_one({"_id": ObjectId(uid)})
        if not user:
            raise ValueError("User not found")

        if not verify_password(request.current_password, user["password_hash"]):
            raise ValueError("Current password is incorrect")

        await collection.update_one(
            {"_id": ObjectId(uid)},
            {
                "$set": {
                    "password_hash": hash_password(request.new_password),
                    "must_change_password": False,
                    "updated_at": datetime.now(timezone.utc),
                }
            },
        )
        logger.info("Password changed for user %s", uid)
        return {"message": "Password changed successfully"}

    # ── Update Role ───────────────────────────────────────────────────────────

    @staticmethod
    async def update_user_role(uid: str, role: str) -> UserResponse:
        """Update a user's role."""
        valid_roles = {
            "platform_admin",
            "tenant_admin",
            "tenant_editor",
            "tenant_viewer",
        }
        if role not in valid_roles:
            raise ValueError(f"Invalid role: {role}")

        db: AsyncIOMotorDatabase = get_db()
        collection = db[USERS_COLLECTION]

        result = await collection.update_one(
            {"_id": ObjectId(uid)},
            {"$set": {"role": role, "updated_at": datetime.now(timezone.utc)}},
        )
        if result.matched_count == 0:
            raise ValueError("User not found")

        return await BuiltinAuthService.get_user_by_uid(uid)
