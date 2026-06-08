from app.repositories.file_repository import FileRepository
from app.repositories.note_repository import NoteRepository
from app.schemas.note_schemas import NoteResponse
from app.services.S3_service import s3_service


class GetUrlFileUseCase:

    def __init__(self, repository_for_notes: NoteRepository, repository_for_files: FileRepository):
        self.repository_for_notes = repository_for_notes
        self.repository_for_files = repository_for_files

    async def execute(self, user_id: str, file_id: str):
        file = await self.repository_for_files.get(file_id)
        if not file:
            raise Exception("File not found")

        note = await self.repository_for_notes.get(str(file.note_id))
        if not note:
            raise Exception("Note not found or User not authenticated")

        if str(note.user_id) != user_id:
            raise Exception("Note not found or User not authenticated")

        key = file.key
        url = await s3_service.generate_presigned_url(key)

        return url
