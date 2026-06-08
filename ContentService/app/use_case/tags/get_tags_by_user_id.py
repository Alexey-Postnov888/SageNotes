from app.models import Tag
from app.repositories.tag_repository import TagRepository


class GetTagsByUserIdUseCase:

    def __init__(self, repository: TagRepository):
        self.repository = repository

    async def execute(self, user_id: str) -> list[Tag]:
        tags = await self.repository.get_all_by_user_id(user_id)
        return tags