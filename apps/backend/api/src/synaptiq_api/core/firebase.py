"""Firebase Admin SDK initialization and utilities."""
import json
import logging
from base64 import b64decode

import firebase_admin
from firebase_admin import auth, credentials

from synaptiq_api.core.config import settings

logger = logging.getLogger(__name__)


def initialize_firebase() -> None:
    """Initialize Firebase Admin SDK with service account credentials."""
    if not settings.firebase_service_account_json:
        logger.warning("Firebase service account JSON not configured. Auth features disabled.")
        return

    try:
        # Decode base64-encoded service account JSON
        decoded = b64decode(settings.firebase_service_account_json)
        service_account = json.loads(decoded)

        # Initialize Firebase Admin SDK
        cred = credentials.Certificate(service_account)
        firebase_admin.initialize_app(cred, {"projectId": settings.firebase_project_id})
        logger.info("Firebase Admin SDK initialized successfully")
    except Exception as e:
        logger.error(f"Failed to initialize Firebase: {e}")
        raise


def verify_firebase_token(token: str) -> dict:
    """
    Verify a Firebase ID token and return decoded claims.

    Args:
        token: Firebase ID token from client

    Returns:
        Decoded token claims (uid, email, custom claims, etc.)

    Raises:
        auth.ExpiredSignatureError: Token is expired
        auth.InvalidSignatureError: Token is invalid
        auth.InvalidIdTokenError: General token validation failure
    """
    try:
        decoded = auth.verify_id_token(token)
        return decoded
    except auth.ExpiredSignatureError:
        logger.warning("Expired Firebase token presented")
        raise
    except auth.InvalidSignatureError:
        logger.warning("Invalid Firebase token signature")
        raise
    except auth.InvalidIdTokenError as e:
        logger.warning(f"Invalid Firebase token: {e}")
        raise


async def get_user_by_email(email: str) -> auth.UserRecord | None:
    """Retrieve a user by email."""
    try:
        return auth.get_user_by_email(email)
    except auth.UserNotFoundError:
        return None


async def set_custom_claims(uid: str, custom_claims: dict) -> None:
    """
    Set custom claims on a Firebase user.

    Custom claims are merged with existing claims.
    """
    existing_user = auth.get_user(uid)
    existing_claims = existing_user.custom_claims or {}
    merged_claims = {**existing_claims, **custom_claims}
    auth.set_custom_user_claims(uid, merged_claims)
    logger.info(f"Updated custom claims for user {uid}: {merged_claims}")


async def create_user(email: str, password: str) -> auth.UserRecord:
    """Create a new Firebase user with email/password."""
    try:
        user = auth.create_user(email=email, password=password)
        logger.info(f"Created Firebase user: {user.uid}")
        return user
    except auth.EmailAlreadyExistsError:
        logger.warning(f"Email already exists: {email}")
        raise
    except Exception as e:
        logger.error(f"Failed to create Firebase user: {e}")
        raise


async def send_email_verification(uid: str) -> str:
    """Generate an email verification link."""
    try:
        link = auth.generate_email_verification_link(uid)
        logger.info(f"Generated email verification link for user {uid}")
        return link
    except Exception as e:
        logger.error(f"Failed to generate email verification link: {e}")
        raise
