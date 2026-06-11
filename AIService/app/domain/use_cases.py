from app.domain.entities import Note, Summary
from app.domain.interfaces import ISummarizer


class SummarizeNoteUseCase:
    """Сценарий: получить суммаризацию заметки."""

    def __init__(self, summarizer: ISummarizer) -> None:
        self._summarizer = summarizer

    async def execute(self, note_id: str, text: str, user_id: str = "anonymous") -> Summary:
        note = Note(note_id=note_id, text=text)
        return await self._summarizer.summarize(note, user_id=user_id)