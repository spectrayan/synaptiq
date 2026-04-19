"""update_schema handler — adds or updates schema fields (T8.2).

Allows tenant admins to modify the catalog schema through the
conversational UI — the same FormInputComponent is used in "admin mode"
to define field labels, types, validation, etc.
"""
import logging

from synaptiq_api.core.mongodb import get_db
from synaptiq_api.services.action_service import ActionRequest, ActionResult
from synaptiq_api.services.schema_service import SchemaService

logger = logging.getLogger(__name__)


async def handle_update_schema(tenant_id: str, request: ActionRequest) -> ActionResult:
    """
    Add or update a field in the tenant's catalog schema.

    Expected ``request.values`` keys:
      - field_id   (str, required)
      - label      (str)
      - type       (str, e.g. text, number, enum)
      - required   (bool)
      - enum_values (list[str], for enum/multi_enum types)
      - designator (str, e.g. title, price, image)
    """
    values = request.values
    field_id = values.get("field_id")

    if not field_id:
        return ActionResult(success=False, message="Missing required field: 'field_id'.")

    # Fetch current schema
    schema = await SchemaService.get_active_schema(tenant_id)
    if not schema:
        return ActionResult(
            success=False,
            message="No active schema found. Import one first.",
        )

    db = get_db()

    # Build the update for the field
    field_update: dict = {k: v for k, v in values.items() if v is not None}
    field_update.setdefault("label", field_id.replace("_", " ").title())

    # Find existing field index
    fields = schema.get("fields", [])
    existing_idx = next(
        (i for i, f in enumerate(fields) if f.get("field_id") == field_id),
        None,
    )

    if existing_idx is not None:
        # Merge updates into existing field
        fields[existing_idx] = {**fields[existing_idx], **field_update}
        action_verb = "updated"
    else:
        # Add new field
        field_update["field_id"] = field_id
        field_update.setdefault("type", "text")
        field_update.setdefault("required", False)
        fields.append(field_update)
        action_verb = "added"

    # Persist
    await db["catalog_schemas"].update_one(
        {"_id": schema["_id"]},
        {"$set": {"fields": fields}},
    )

    label = field_update.get("label", field_id)
    return ActionResult(
        success=True,
        message=f'Field "{label}" ({field_id}) {action_verb} in the catalog schema.',
        data={"field_id": field_id, "action": action_verb},
        suggestions=[
            {"label": "Add another field", "prompt": "Add a new schema field"},
            {"label": "View schema", "prompt": "Show catalog schema"},
        ],
    )
