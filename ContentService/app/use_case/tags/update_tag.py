from app.repositories.tag_repository import TagRepository
from app.schemas.tag_schemas import TagUpdate


class UpdateTagUseCase:
    def __init__(self, repository: TagRepository):
        self.repository = repository

    async def execute(self, user_id: str, tag_id: str, data: TagUpdate):
        tag = await self.repository.get(tag_id)
        if not tag:
            return None

        if str(tag.user_id) != user_id:
            return None
        return await self.repository.update(tag_id, data)