import httpx
from temporalio import activity

from app.schemas.index_request import IndexRequest
from app.schemas.ocr_file import OcrRequest

INDEX_SERVICE_URL = "http://index-service:8080/index"
OCR_SERVICE_URL = "http://ocr-service:8080/api/ocr"


@activity.define(name="process_ocr_activity")
async def process_ocr_activity(ocr_request: OcrRequest) -> str:
    if len(ocr_request.files) == 0:
        return ""

    async with httpx.AsyncClient(timeout=300.0) as client:
        try:
            response = await client.post(OCR_SERVICE_URL, json=ocr_request.dict(exclude_none=True))

            response.raise_for_status()

            data = response.json()
            return data.get("text", "")

        except httpx.HTTPStatusError as e:
            activity.logger.error(f"OCR Service returned status error: {e}")
            raise e
        except httpx.RequestError as e:
            activity.logger.error(f"Network error talking to OCR Service: {e}")
            raise e


@activity.define(name="index_document_activity")
async def index_document_activity(index_request: IndexRequest) -> str:
    async with httpx.AsyncClient(timeout=30.0) as client:
        response = await client.post(INDEX_SERVICE_URL, json=index_request.dict(exclude_none=True))
        response.raise_for_status()
        return "Indexed successfully"