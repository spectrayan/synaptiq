"""Base models and mixins for MongoDB-backed Pydantic schemas."""
from datetime import datetime
from typing import Annotated, Any

from bson import ObjectId
from pydantic import BaseModel, ConfigDict, Field, field_serializer, field_validator


class PyObjectId(ObjectId):
    """Custom ObjectId type for Pydantic v2 compatibility."""

    @classmethod
    def __get_pydantic_core_schema__(cls, _source_type: Any, _handler: Any) -> Any:  # noqa: ANN401
        from pydantic_core import core_schema

        return core_schema.no_info_plain_validator_function(
            cls.validate,
            serialization=core_schema.to_string_ser_schema(),
        )

    @classmethod
    def validate(cls, v: Any) -> ObjectId:  # noqa: ANN401
        if isinstance(v, ObjectId):
            return v
        if isinstance(v, str) and ObjectId.is_valid(v):
            return ObjectId(v)
        raise ValueError(f"Invalid ObjectId: {v}")


class MongoBaseModel(BaseModel):
    """Base model with MongoDB document conventions."""

    model_config = ConfigDict(
        populate_by_name=True,
        arbitrary_types_allowed=True,
        json_encoders={ObjectId: str, datetime: lambda v: v.isoformat()},
    )

    id: Annotated[PyObjectId | None, Field(alias="_id", default=None)]

    @field_serializer("id")
    def serialize_id(self, v: PyObjectId | None) -> str | None:
        return str(v) if v else None


class TimestampMixin(BaseModel):
    """Mixin that adds created_at / updated_at fields."""

    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)


class TenantScopedMixin(BaseModel):
    """Mixin that adds tenant_id scoping to any collection."""

    tenant_id: str = Field(..., min_length=1, description="Owning tenant identifier")
