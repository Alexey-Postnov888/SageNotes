from app.repositories.note_repository import NoteRepository
from app.models import Note


class GetNotesUseCase:

    def __init__(self, repository: NoteRepository):
        self.repository = repository

    async def execute(self) -> list[Note]:
        return await self.repository.get_all()