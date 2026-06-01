from datetime import datetime
from uuid import UUID

from pydantic import BaseModel, ConfigDict

class FileResponse(BaseModel):
    id: UUID

    name: str
    url: str

    extension: str
    mime_type: str
    size: int

    created_at: datetime

    model_config = ConfigDict(from_attributes=True)
