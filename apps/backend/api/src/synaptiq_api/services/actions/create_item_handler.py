"""create_item handler — validates form data and creates a catalog item (T8.2).

Uses the existing ``CatalogService.create_item`` which already handles
schema validation, coercion, and tenant limit enforcement.
"""
import logging

from synaptiq_api.services.action_service import ActionRequest, ActionResult
from synaptiq_api.services.catalog_service import CatalogService

logger = logging.getLogger(__name__)


async def handle_create_item(tenant_id: str, request: ActionRequest) -> ActionResult:
    """
    Create a single catalog item from DSL form submission.

    Expected ``request.values`` keys match the tenant's schema field_ids
    (e.g. name, price, category, description).
    """
    data = request.values

    if not data:
        return ActionResult(success=False, message="No item data provided.")

    try:
        item = await CatalogService.create_item(tenant_id, data)
    except ValueError as exc:
        return ActionResult(success=False, message=str(exc))

    item_id = item.get("_id", "")
    name = data.get("name", item_id)

    return ActionResult(
        success=True,
        message=f'Product "{name}" created successfully.',
        data={"item_id": item_id},
        suggestions=[
            {"label": "Add another", "prompt": "Add a new product"},
            {"label": "View catalog", "prompt": "Show all products"},
            {"label": "Edit this item", "prompt": f"Edit item {item_id}"},
        ],
    )
