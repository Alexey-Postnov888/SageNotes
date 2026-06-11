import base64
import json
import traceback

from fastapi import Depends
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jose import jwt

from app.domain.interfaces import ISummarizer
from app.domain.use_cases import SummarizeNoteUseCase
from app.infrastructure.llm_client import YandexCloudSummarizer
from app.infrastructure.rabbitmq_publisher import RabbitMQPublisher
from app.infrastructure.auth import keycloak_auth, rsa_public_key_from_jwk

_rabbitmq = RabbitMQPublisher()
_summarizer: ISummarizer = YandexCloudSummarizer(rabbitmq=_rabbitmq)
_summarize_use_case = SummarizeNoteUseCase(summarizer=_summarizer)

security = HTTPBearer(auto_error=False)


def get_summarize_use_case() -> SummarizeNoteUseCase:
    return _summarize_use_case


def get_optional_user_id(
    credentials: HTTPAuthorizationCredentials | None = Depends(security),
) -> str:
    print(f"🔍 credentials = {credentials is not None}")
    
    if credentials:
        token = credentials.credentials
        print(f"🔍 token prefix = {token[:50]}...")

        try:
            headers = jwt.get_unverified_headers(token)
            kid = headers.get('kid')
            print(f"🔍 kid = {kid}")
            print(f"🔍 issuer = {keycloak_auth.issuer}")
            print(f"🔍 issuer_fallback = {keycloak_auth.issuer_fallback}")
            print(f"🔍 audience = {keycloak_auth.audience}")
            print(f"🔍 jwks keys count = {len(keycloak_auth.jwks.get('keys', []))}")

            if kid:
                for key in keycloak_auth.jwks.get('keys', []):
                    if key.get('kid') == kid:
                        public_key = rsa_public_key_from_jwk(key)
                        print(f"🔍 Нашли ключ, пробуем декодировать...")
                        for issuer in [keycloak_auth.issuer, keycloak_auth.issuer_fallback]:
                            try:
                                payload = jwt.decode(
                                    token, public_key, algorithms=['RS256'],
                                    audience=keycloak_auth.audience, issuer=issuer,
                                    options={"verify_signature": True, "verify_aud": True, "verify_exp": True}
                                )
                                user_id = payload.get('azp')
                                print(f"🔍 payload keys = {list(payload.keys())}")
                                print(f"🔍 payload = {payload}")
                                print(f"✅ Успех! user_id = {user_id}")
                                return str(user_id)
                            except Exception as e:
                                print(f"❌ Ошибка с issuer={issuer}: {e}")
        except Exception as e:
            print(f"❌ Ошибка Keycloak: {e}")
            traceback.print_exc()

        try:
            payload_b64 = token.split(".")[1]
            payload_b64 += "=" * (4 - len(payload_b64) % 4)
            payload = json.loads(base64.b64decode(payload_b64))
            user_id = payload.get("sub")
            print(f"🔍 Fallback: sub = {user_id}")
            if user_id:
                return str(user_id)
        except Exception as e:
            print(f"❌ Ошибка fallback: {e}")

    print("⚠️ Возвращаем anonymous")
    return "anonymous"