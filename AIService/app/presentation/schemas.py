from pydantic import BaseModel, Field


class SummarizeRequest(BaseModel):
    note_id: str = Field(..., description="Идентификатор заметки", examples=["abc-123"])
    text: str = Field(..., description="Полный текст заметки для суммаризации", min_length=1)


class SummarizeResponse(BaseModel):
    note_id: str = Field(..., description="Идентификатор заметки")
    summary: str = Field(..., description="Текст суммаризации")


class TokenRequest(BaseModel):
    username: str = Field(default="", description="Логин от Keycloak")
    password: str = Field(default="", description="Пароль от Keycloak")
    client_id: str = Field(default="admin-cli", description="Client ID")


class TokenResponse(BaseModel):
    access_token: str = Field(default="", description="JWT токен")
    error: str = Field(default="", description="Ошибка, если есть")