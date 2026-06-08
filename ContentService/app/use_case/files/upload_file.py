import os
from datetime import datetime
from uuid import UUID

from botocore.exceptions import ClientError
from fastapi import UploadFile as FastAPIFile

from app.repositories.file_repository import FileRepository
from app.repositories.note_repository import NoteRepository
from app.schemas.file_schemas import FileCreate, FileResponse
from app.services.S3_service import s3_service


class UploadFileUseCase:

    def __init__(self, repository_for_notes: NoteRepository, repository_for_files: FileRepository):
        self.repository_for_notes = repository_for_notes
        self.repository_for_files = repository_for_files

    async def execute(self, user_id: str, note_id: str, file: FastAPIFile):

        note = await self.repository_for_notes.get(note_id)

        if not note:
            raise Exception("Note not found or User not authenticated")

        if str(note.user_id) != user_id:
            raise Exception("Note not found or User not authenticated")


        try:
            key = await s3_service.upload_file(note_id, file)
            file_create = FileCreate(
                note_id=UUID(note_id),
                name=file.filename or "",
                key=key,
                extension=os.path.splitext(str(file.filename))[1] or "",
                mime_type=str(file.content_type),
                size=file.size or 0
            )

            file_db = await self.repository_for_files.create(file_create)
            file_response = FileResponse(
                id=file_db.id,
                name=str(file_db.name),
                extension=str(file_db.extension),
                mime_type=str(file_db.mime_type),
                size=int(getattr(file_db, "size", 0)),
                created_at=getattr(file_db, "created_at", datetime.now()),
            )

            return file_response

        except ClientError as e:
            raise e