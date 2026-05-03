"""Firestore integration for workflow specifications."""

from __future__ import annotations

import json
import os
from typing import Any, Dict, Optional

from google.cloud import firestore

from ..builder.models.settings import FlowSettings


class FirestoreWorkflowLoader:
    """Load workflow specifications from Firestore."""
    
    def __init__(self, project_id: Optional[str] = None):
        self.project_id = project_id or os.environ.get("GOOGLE_CLOUD_PROJECT")
        self.db = firestore.Client(project=self.project_id)
    
    def load_workflow(self, tenant_id: str, workflow_id: str, version: Optional[str] = None) -> FlowSettings:
        """Load a workflow specification from Firestore.
        
        Args:
            tenant_id: Tenant identifier
            workflow_id: Workflow identifier
            version: Specific version (defaults to latest)
            
        Returns:
            FlowSettings object
        """
        # Path: tenants/{tenant_id}/workflows/{workflow_id}/versions/{version}
        workflow_ref = self.db.collection("tenants").document(tenant_id).collection("workflows").document(workflow_id)
        
        if version:
            version_ref = workflow_ref.collection("versions").document(version)
        else:
            # Get latest version
            versions = workflow_ref.collection("versions").order_by("created_at", direction=firestore.Query.DESCENDING).limit(1)
            version_docs = list(versions.stream())
            if not version_docs:
                raise ValueError(f"No versions found for workflow {workflow_id}")
            version_ref = version_docs[0].reference
        
        version_doc = version_ref.get()
        if not version_doc.exists:
            raise ValueError(f"Workflow version not found: {tenant_id}/{workflow_id}/{version}")
        
        workflow_data = version_doc.to_dict()
        
        # Convert Firestore document to FlowSettings
        return FlowSettings.model_validate(workflow_data["specification"])
    
    def save_workflow(self, tenant_id: str, workflow_id: str, flow_settings: FlowSettings, version: str) -> None:
        """Save a workflow specification to Firestore.
        
        Args:
            tenant_id: Tenant identifier
            workflow_id: Workflow identifier
            flow_settings: Workflow specification
            version: Version identifier
        """
        import datetime
        
        workflow_ref = self.db.collection("tenants").document(tenant_id).collection("workflows").document(workflow_id)
        version_ref = workflow_ref.collection("versions").document(version)
        
        version_data = {
            "specification": flow_settings.model_dump(),
            "created_at": datetime.datetime.utcnow(),
            "version": version,
            "workflow_id": workflow_id,
            "tenant_id": tenant_id,
        }
        
        version_ref.set(version_data)
    
    def list_workflows(self, tenant_id: str) -> list[Dict[str, Any]]:
        """List all workflows for a tenant."""
        workflows_ref = self.db.collection("tenants").document(tenant_id).collection("workflows")
        workflows = []
        
        for workflow_doc in workflows_ref.stream():
            workflow_data = workflow_doc.to_dict()
            workflows.append({
                "id": workflow_doc.id,
                "name": workflow_data.get("name"),
                "description": workflow_data.get("description"),
                "created_at": workflow_data.get("created_at"),
                "updated_at": workflow_data.get("updated_at"),
            })
        
        return workflows