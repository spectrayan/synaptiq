"""save_item handler — persists bookmarked catalog items (T8.1).

When an end-user clicks "Save" on a catalog item, the item snapshot is
stored in the ``saved_items`` collection keyed by session_id (and
optionally user_uid for authenticated users).
"""
import logging
from datetime import datetime

from synaptiq_api.core.mongodb import get_db
from synaptiq_api.services.action_service import ActionRequest, ActionResult

logger = logging.getLogger(__name__)

SAVED_ITEMS_COLLECTION = "saved_items"
CATALOG_ITEMS_COLLECTION = "catalog_items"


async def handle_save_item(tenant_id: str, request: ActionRequest) -> ActionResult:
    """
    Save/bookmark a catalog item for later retrieval.

    Expected ``request.values`` keys:
      - item_id   (str, required)

    Expected ``request.metadata`` keys:
      - session_id (str, required)
      - user_uid   (str, optional — set when user is authenticated)
    """
    item_id = request.values.get("item_id")
    session_id = request.metadata.get("session_id")

    if not item_id:
        return ActionResult(success=False, message="Missing required field: 'item_id'.")

    if not session_id:
        return ActionResult(success=False, message="Missing session_id in metadata.")

    db = get_db()

    # Check for duplicate — same item + session
    existing = await db[SAVED_ITEMS_COLLECTION].find_one({
        "tenant_id": tenant_id,
        "item_id": item_id,
        "session_id": session_id,
    })
    if existing:
        return ActionResult(
            success=True,
            message="This item is already in your saved list.",
            data={"item_id": item_id, "already_saved": True},
            suggestions=[
                {"label": "View saved items", "prompt": "Show my saved items"},
                {"label": "Continue browsing", "prompt": "Search products"},
            ],
        )

    # Fetch the full catalog item to snapshot
    catalog_item = await db[CATALOG_ITEMS_COLLECTION].find_one({
        "tenant_id": tenant_id,
        "_id": item_id,
    })

    if not catalog_item:
        # Fallback: try matching by string id field
        catalog_item = await db[CATALOG_ITEMS_COLLECTION].find_one({
            "tenant_id": tenant_id,
            "item_id": item_id,
        })

    # Build snapshot — use catalog item data or the provided values as fallback
    item_snapshot = {}
    if catalog_item:
        # Remove internal fields from snapshot
        item_snapshot = {
            k: v for k, v in catalog_item.items()
            if k not in ("_id", "tenant_id", "embedding", "status")
        }
    else:
        # Use values directly (item might have been provided inline)
        item_snapshot = {k: v for k, v in request.values.items() if k != "item_id"}

    user_uid = request.metadata.get("user_uid")

    doc = {
        "tenant_id": tenant_id,
        "item_id": item_id,
        "session_id": session_id,
        "user_uid": user_uid,
        "item_snapshot": item_snapshot,
        "created_at": datetime.utcnow(),
    }

    result = await db[SAVED_ITEMS_COLLECTION].insert_one(doc)

    item_name = item_snapshot.get("name", item_snapshot.get("title", item_id))

    logger.info(
        "Saved item %s (doc %s) for session %s, tenant %s",
        item_id, result.inserted_id, session_id, tenant_id,
    )

    return ActionResult(
        success=True,
        message=f'"{item_name}" has been saved to your list! ✨',
        data={"item_id": item_id, "saved_item_id": str(result.inserted_id)},
        suggestions=[
            {"label": "View saved items", "prompt": "Show my saved items"},
            {"label": "Continue browsing", "prompt": "Search products"},
        ],
    )
