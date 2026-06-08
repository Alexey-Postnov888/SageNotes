from sqlalchemy import select

from app.models import Tag
from app.repositories.base_repository import BaseRepository
from app.schemas.tag_schemas import TagCreate, TagUpdate


class TagRepository(
    BaseRepository[Tag, TagCreate, TagUpdate]
):

    model_cls = Tag

    async def get_all_by_user_id(self, user_id: str) -> list[Tag]:
        async with self._session() as session:
            result = await session.execute(
                select(self.model_cls)
                .where(self.model_cls.user_id == user_id)
            )

            return list(result.scalars().all())