from temporalio.client import Client
from fastapi import APIRouter, Depends
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

from app.presentation.dependencies import get_optional_user_id
from app.presentation.schemas import SummarizeRequest, SummarizeResponse

router = APIRouter(prefix="/summary", tags=["summary"])
security = HTTPBearer(auto_error=False)

TASK_QUEUE = "summarization-task-queue"


@router.post("/", response_model=SummarizeResponse)
async def summarize_note(
    request: SummarizeRequest,
    credentials: HTTPAuthorizationCredentials | None = Depends(security),
    user_id: str = Depends(get_optional_user_id),
) -> SummarizeResponse:
    """Запуск Saga через Temporal."""
    client = await Client.connect("temporal:7233", namespace="default")

    result = await client.execute_workflow(
        "SummarizationWorkflow",
        args=[request.note_id, request.text, user_id],
        id=f"summary-{request.note_id}",
        task_queue=TASK_QUEUE,
    )

    return SummarizeResponse(note_id=result["note_id"], summary=result["summary"])