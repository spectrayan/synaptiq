import json
import logging
from typing import Any
from langchain_core.tools import tool
from langchain_core.runnables import RunnableConfig

from synaptiq_api.services.search_service import catalog_search as do_catalog_search
from synaptiq_api.services.schema_registry import SchemaRegistry
from synaptiq_api.services.embedding_service import generate_embedding

logger = logging.getLogger(__name__)

@tool
async def search_catalog(query: str, config: RunnableConfig) -> str:
    """
    Search the standard product catalog using semantic vector search.
    Use this tool when the user queries about products, items, or general catalog inventory.
    """
    tenant_id = config["configurable"].get("tenant_id")
    if not tenant_id:
        return "Error: tenant_id not found in configuration."

    logger.info("Agent computing embedding for catalog search: %s", query)
    embedding = await generate_embedding(query)
    if not embedding:
        return "Error: could not generate embedding for search query."

    logger.info("Agent executing catalog search for tenant %s", tenant_id)
    results = await do_catalog_search(
        tenant_id=tenant_id,
        query_embedding=embedding,
        top_k=8,
        min_score=0.3
    )

    if not results:
        return "No relevant catalog items found."

    # Format the results cleanly
    items_text = []
    for i, item in enumerate(results, 1):
        data = item.get("data", {})
        score = item.get("score", 0)
        fields = " | ".join(f"{k}: {v}" for k, v in data.items() if v)
        items_text.append(f"[{i}] (score: {score:.2f}) {fields}")

    return "Relevant Catalog Items:\n" + "\n".join(items_text)

@tool
async def query_collection(collection_name: str, mongodb_pipeline: list[dict], config: RunnableConfig) -> str:
    """
    Execute a dynamic MongoDB aggregation pipeline against a specific data collection.
    Use this tool to fetch metrics, summarize tasks, group orders, or retrieve any non-catalog data.
    The response will be JSON query results.

    IMPORTANT: For collections with many documents (sales_metrics has 90 daily rows, orders has 30+),
    ALWAYS use $group, $sort, and/or $limit stages to summarize data rather than returning raw rows.
    For example, group by month and sum revenue instead of returning 90 individual daily records.
    The maximum number of returned records is capped at 50.

    Args:
        collection_name: The name of the collection to query (e.g. 'sales_metrics', 'tasks', 'orders', 'support_tickets')
        mongodb_pipeline: A list of dicts representing a MongoDB aggregation pipeline. (e.g., [{"$group": {"_id": "$category", "total": {"$sum": "$revenue"}}}, {"$sort": {"total": -1}}, {"$limit": 10}])
    """
    tenant_id = config["configurable"].get("tenant_id")
    if not tenant_id:
        return "Error: tenant_id not found in configuration."

    logger.info("Agent querying collection %s for tenant %s", collection_name, tenant_id)
    try:
        # Enforce a $limit safety cap if no explicit limit is in the pipeline
        has_limit = any("$limit" in stage for stage in mongodb_pipeline)
        if not has_limit:
            mongodb_pipeline.append({"$limit": 50})

        # Note: SchemaRegistry automatically prepends a $match for tenant_id.
        result = await SchemaRegistry.query_data(
            tenant_id=tenant_id,
            collection_name=collection_name,
            pipeline=mongodb_pipeline
        )
        data = result.get("results", [])
        total = result.get("total", len(data))
        output = json.dumps(data, default=str)
        if total > len(data):
            output += f"\n\n(Showing {len(data)} of {total} total records. Use $group to summarize large datasets.)"
        return output
    except Exception as e:
        logger.error("Error executing query against %s: %s", collection_name, str(e))
        return f"Error executing query: {str(e)}"


# ---------------------------------------------------------------------------
# P1-A: Admin Chat Tools — allow admin to mutate config through conversation
# ---------------------------------------------------------------------------

ALLOWED_CONFIG_SECTIONS = frozenset({
    "ai_persona", "ai_guardrails", "branding", "components", "actions",
    "llm_provider",
})


@tool
async def update_tenant_config(section: str, updates: dict, config: RunnableConfig) -> str:
    """
    Update a section of the tenant's configuration.
    Use this tool when an ADMIN asks to change AI persona, branding, guardrails, or component settings.
    Do NOT use for regular users — only when the user has admin privileges.

    Args:
        section: Config section to update. One of:
                 'ai_persona' — display_name, tone, custom_instruction, welcome_message, starter_prompts
                 'ai_guardrails' — out_of_scope_message, recommendation_mode, language
                 'branding' — primary_color, secondary_color, background_style
                 'components' — item_card, item_grid, chart, etc. (boolean toggles)
                 'actions' — enquiry_webhook_url, enquiry_email
                 'llm_provider' — provider, model_id
        updates: Key-value pairs to update within that section.
                 Example: {"display_name": "Aria", "tone": "friendly"}
    """
    tenant_id = config["configurable"].get("tenant_id")
    if not tenant_id:
        return "Error: tenant_id not found in configuration."

    if section not in ALLOWED_CONFIG_SECTIONS:
        return f"Error: Invalid config section '{section}'. Allowed: {', '.join(sorted(ALLOWED_CONFIG_SECTIONS))}"

    if not updates:
        return "Error: No updates provided."

    logger.info("Admin tool: updating %s for tenant %s: %s", section, tenant_id, list(updates.keys()))

    try:
        from synaptiq_api.services.tenant_service import TenantService

        # Build dot-notation $set operations (same pattern as config.py)
        set_ops = {}
        for key, value in updates.items():
            if value is not None:
                set_ops[f"config.{section}.{key}"] = value

        if not set_ops:
            return "Error: All update values were None."

        success = await TenantService.update_tenant(tenant_id, set_ops)

        if not success:
            return f"Error: Tenant '{tenant_id}' not found or no changes applied."

        # Also invalidate the prompt cache so the next message uses the new config
        try:
            from synaptiq_api.core.redis import get_redis
            redis = get_redis()
            await redis.delete(f"prompt:{tenant_id}:admin", f"prompt:{tenant_id}:owner", f"prompt:{tenant_id}:user")
        except Exception:
            pass

        changed = ", ".join(f"{k}={v}" for k, v in updates.items())
        return f"Successfully updated {section}: {changed}"

    except Exception as e:
        logger.error("Admin tool update_tenant_config error: %s", e)
        return f"Error updating config: {str(e)}"


@tool
async def manage_schema_field(operation: str, field_id: str, field_config: dict | None, config: RunnableConfig) -> str:
    """
    Add, update, or remove a field in the tenant's catalog schema.
    Use this when an ADMIN asks to modify the catalog schema.

    Args:
        operation: One of 'add', 'update', 'remove'
        field_id: The field identifier (snake_case, e.g. 'warranty_months')
        field_config: Field configuration dict. Required for 'add' and 'update'. Keys:
                      label (str), type (text|number|currency|enum|boolean|date|url|image),
                      required (bool), searchable (bool), filterable (bool), sortable (bool),
                      enum_values (list of strings, for enum type)
    """
    tenant_id = config["configurable"].get("tenant_id")
    if not tenant_id:
        return "Error: tenant_id not found in configuration."

    if operation not in ("add", "update", "remove"):
        return f"Error: Invalid operation '{operation}'. Must be 'add', 'update', or 'remove'."

    if not field_id:
        return "Error: field_id is required."

    logger.info("Admin tool: %s schema field '%s' for tenant %s", operation, field_id, tenant_id)

    try:
        from synaptiq_api.core.mongodb import get_db
        from synaptiq_api.services.schema_service import SchemaService

        schema = await SchemaService.get_active_schema(tenant_id)
        if not schema:
            return "Error: No active schema found. Import or create one first."

        db = get_db()
        fields = schema.get("fields", [])

        if operation == "remove":
            original_len = len(fields)
            fields = [f for f in fields if f.get("field_id") != field_id]
            if len(fields) == original_len:
                return f"Field '{field_id}' not found in schema."
            await db["catalog_schemas"].update_one(
                {"_id": schema["_id"]},
                {"$set": {"fields": fields}},
            )
            return f"Field '{field_id}' removed from schema."

        if not field_config:
            return f"Error: field_config is required for '{operation}' operation."

        field_update = {k: v for k, v in field_config.items() if v is not None}
        field_update["field_id"] = field_id
        field_update.setdefault("label", field_id.replace("_", " ").title())

        existing_idx = next(
            (i for i, f in enumerate(fields) if f.get("field_id") == field_id),
            None,
        )

        if operation == "add":
            if existing_idx is not None:
                return f"Field '{field_id}' already exists. Use 'update' to modify it."
            if len(fields) >= 50:
                return "Error: Maximum 50 fields allowed per schema."
            field_update.setdefault("type", "text")
            field_update.setdefault("required", False)
            fields.append(field_update)
        elif operation == "update":
            if existing_idx is None:
                return f"Field '{field_id}' not found. Use 'add' to create it."
            fields[existing_idx] = {**fields[existing_idx], **field_update}

        await db["catalog_schemas"].update_one(
            {"_id": schema["_id"]},
            {"$set": {"fields": fields}},
        )

        label = field_update.get("label", field_id)
        verb = "added" if operation == "add" else "updated"
        return f"Field '{label}' ({field_id}) {verb} successfully."

    except Exception as e:
        logger.error("Admin tool manage_schema_field error: %s", e)
        return f"Error managing schema field: {str(e)}"


@tool
async def create_catalog_item(item_data: dict, config: RunnableConfig) -> str:
    """
    Create a new catalog item with the provided data.
    Use this when an ADMIN asks to add a product/item to the catalog via chat.

    Args:
        item_data: Dict of field values matching the tenant's schema.
                   Example: {"name": "Widget Pro", "price": 29.99, "category": "Electronics"}
    """
    tenant_id = config["configurable"].get("tenant_id")
    if not tenant_id:
        return "Error: tenant_id not found in configuration."

    if not item_data:
        return "Error: No item data provided."

    logger.info("Admin tool: creating catalog item for tenant %s: %s", tenant_id, list(item_data.keys()))

    try:
        from synaptiq_api.services.catalog_service import CatalogService

        item = await CatalogService.create_item(tenant_id, item_data)
        item_id = item.get("_id", item.get("item_id", "unknown"))
        name = item_data.get("name", item_data.get("title", str(item_id)))
        return f"Product '{name}' created successfully (ID: {item_id})."

    except ValueError as exc:
        return f"Validation error: {str(exc)}"
    except Exception as e:
        logger.error("Admin tool create_catalog_item error: %s", e)
        return f"Error creating item: {str(e)}"
