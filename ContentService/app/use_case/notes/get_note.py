from app.repositories.note_repository import NoteRepository

class GetNoteUseCase:

    def __init__(self, repository: NoteRepository):
        self.repository = repository

    async def execute(self, note_id: str):
        return await self.repository.get(note_id)