import requests
from fastapi import APIRouter, Depends
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

from app.config import settings
from app.domain.use_cases import SummarizeNoteUseCase
from app.presentation.dependencies import get_summarize_use_case, get_optional_user_id
from app.presentation.schemas import SummarizeRequest, SummarizeResponse, TokenRequest, TokenResponse

router = APIRouter(prefix="/summary", tags=["summary"])
security = HTTPBearer(auto_error=False)


@router.post("/auth/token", response_model=TokenResponse)
async def get_token(request: TokenRequest) -> TokenResponse:
    """Получить JWT-токен из Keycloak."""
    username = request.username or settings.keycloak_admin
    password = request.password or settings.keycloak_admin_password
    client_id = request.client_id or "admin-cli"

    response = requests.post(
        f"{settings.keycloak_server_url}/realms/{settings.keycloak_realm}/protocol/openid-connect/token",
        data={
            "client_id": client_id,
            "username": username,
            "password": password,
            "grant_type": "password",
        },
        headers={"Content-Type": "application/x-www-form-urlencoded"},
    )
    if response.status_code != 200:
        return TokenResponse(access_token="", error=response.text)

    data = response.json()
    return TokenResponse(access_token=data.get("access_token", ""))


@router.post("/", response_model=SummarizeResponse)
async def summarize_note(
    request: SummarizeRequest,
    use_case: SummarizeNoteUseCase = Depends(get_summarize_use_case),
    credentials: HTTPAuthorizationCredentials | None = Depends(security),
    user_id: str = Depends(get_optional_user_id),
) -> SummarizeResponse:
    result = await use_case.execute(
        note_id=request.note_id,
        text=request.text,
        user_id=user_id,
    )
    return SummarizeResponse(note_id=result.note_id, summary=result.summary)