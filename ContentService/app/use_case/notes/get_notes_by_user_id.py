from app.repositories.note_repository import NoteRepository
from app.models import Note


class GetNotesByUserIdUseCase:

    def __init__(self, repository: NoteRepository):
        self.repository = repository

    async def execute(self, user_id: str) -> list[Note]:
        notes = await self.repository.get_all_by_user_id(user_id)
        return notes