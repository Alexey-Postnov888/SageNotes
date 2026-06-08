from uuid import UUID

from app.repositories.tag_repository import TagRepository
from app.schemas.tag_schemas import TagCreate


class CreateTagUseCase:
    def __init__(self, repository: TagRepository):
        self.repository = repository

    async def execute(self,user_id: str, data: TagCreate):
        data.user_id = UUID(user_id)
        return await self.repository.create(data)