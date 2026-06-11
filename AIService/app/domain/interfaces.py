from abc import ABC, abstractmethod

from app.domain.entities import Note, Summary


class ISummarizer(ABC):
    @abstractmethod
    async def summarize(self, note: Note, user_id: str = "anonymous") -> Summary:
        ...