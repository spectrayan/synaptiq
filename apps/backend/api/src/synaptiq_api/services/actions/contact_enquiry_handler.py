"""contact_enquiry handler — logs enquiry/lead capture (T8.2).

When a visitor fills in a contact form (e.g. "enquire about this product"),
the event is persisted to the ``enquiries`` collection and optionally
forwarded to a webhook configured per tenant.
"""
import logging
from datetime import datetime

from synaptiq_api.core.mongodb import get_db
from synaptiq_api.services.action_service import ActionRequest, ActionResult

logger = logging.getLogger(__name__)

ENQUIRIES_COLLECTION = "enquiries"


async def handle_contact_enquiry(tenant_id: str, request: ActionRequest) -> ActionResult:
    """
    Save an enquiry/lead capture form submission.

    Expected ``request.values`` keys:
      - name     (str)
      - email    (str)
      - phone    (str, optional)
      - message  (str)
      - item_id  (str, optional — the product they're enquiring about)
    """
    values = request.values
    name = values.get("name", "").strip()
    email = values.get("email", "").strip()
    message = values.get("message", "").strip()

    if not name or not email:
        return ActionResult(
            success=False,
            message="Name and email are required for enquiries.",
        )

    db = get_db()

    doc = {
        "tenant_id": tenant_id,
        "name": name,
        "email": email,
        "phone": values.get("phone", ""),
        "message": message,
        "item_id": values.get("item_id"),
        "session_id": request.metadata.get("session_id"),
        "status": "new",
        "created_at": datetime.utcnow(),
    }
    result = await db[ENQUIRIES_COLLECTION].insert_one(doc)

    logger.info(
        "Enquiry %s from %s for tenant %s",
        result.inserted_id, email, tenant_id,
    )

    # TODO: fire tenant webhook if configured (Phase 12)

    return ActionResult(
        success=True,
        message=f"Thanks {name}! Your enquiry has been received. We'll be in touch shortly.",
        data={"enquiry_id": str(result.inserted_id)},
        suggestions=[
            {"label": "Browse catalog", "prompt": "Show me products"},
            {"label": "Ask a question", "prompt": "Tell me about your top products"},
        ],
    )
