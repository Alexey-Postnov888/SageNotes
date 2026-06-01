from uuid import UUID

from pydantic import BaseModel, ConfigDict


class TagBase(BaseModel):
    title: str
    color: str


class TagResponse(TagBase):
    id: UUID

    model_config = ConfigDict(from_attributes=True)
