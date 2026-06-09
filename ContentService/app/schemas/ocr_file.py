from typing import List

from pydantic import BaseModel


class OcrFile (BaseModel):
    fid: str
    url: str

class OcrRequest (BaseModel):
    note_id: str
    files: List[OcrFile]