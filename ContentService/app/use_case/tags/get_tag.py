from app.repositories.tag_repository import TagRepository


class GetTagUseCase:
    def __init__(self, repository: TagRepository):
        self.repository = repository

    async def execute(self, user_id: str, tag_id):
        tag = await self.repository.get(tag_id)

        if not tag:
            return None

        if str(tag.user_id) != user_id:
            return None

        return await self.repository.get(tag_id)