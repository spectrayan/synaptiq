"""Action handlers — Phase 8 (T8.2).

Auto-registers all handlers on import.
"""
from synaptiq_api.services.action_service import register_handler
from synaptiq_api.services.actions.create_item_handler import handle_create_item
from synaptiq_api.services.actions.update_schema_handler import handle_update_schema
from synaptiq_api.services.actions.contact_enquiry_handler import handle_contact_enquiry

# Register all handlers so the dispatcher can find them
register_handler("create_item", handle_create_item)
register_handler("update_schema", handle_update_schema)
register_handler("contact_enquiry", handle_contact_enquiry)
