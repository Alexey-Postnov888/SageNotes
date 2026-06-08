from app.repositories.note_repository import NoteRepository

class GetNoteUseCase:

    def __init__(self, repository: NoteRepository):
        self.repository = repository

    async def execute(self, user_id:str, note_id: str):
        note = await self.repository.get(note_id)

        if not note:
            return None

        if str(note.user_id) != user_id:
            return None

        return note