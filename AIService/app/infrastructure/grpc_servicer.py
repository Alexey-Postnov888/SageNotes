import base64
import json

import grpc
from jose import jwt

from app.proto import summary_pb2, summary_pb2_grpc  # type: ignore
from app.domain.entities import Note
from app.infrastructure.auth import keycloak_auth, rsa_public_key_from_jwk
from app.exceptions import NoteTextEmptyError, SummarizationFailedError


class SummaryGrpcServicer(summary_pb2_grpc.SummaryServiceServicer):
    """gRPC-сервис для суммаризации."""

    def __init__(self, use_case):
        self._use_case = use_case

    def _get_user_id_from_metadata(self, context: grpc.aio.ServicerContext) -> str:
        metadata = context.invocation_metadata() or []
        for key, value in metadata:
            if key == "authorization":
                token = value.removeprefix("Bearer ")
                try:
                    headers = jwt.get_unverified_headers(token)
                    kid = headers.get('kid')
                    if kid:
                        for k in keycloak_auth.jwks.get('keys', []):
                            if k.get('kid') == kid:
                                public_key = rsa_public_key_from_jwk(k)
                                for issuer in [keycloak_auth.issuer, keycloak_auth.issuer_fallback]:
                                    try:
                                        payload = jwt.decode(
                                            token, public_key, algorithms=['RS256'],
                                            audience=keycloak_auth.audience, issuer=issuer,
                                            options={"verify_signature": True, "verify_aud": True, "verify_exp": True}
                                        )
                                        return payload.get('sub') or payload.get('azp', 'anonymous')
                                    except Exception:
                                        continue
                except Exception:
                    pass

                try:
                    payload_b64 = token.split(".")[1]
                    payload_b64 += "=" * (4 - len(payload_b64) % 4)
                    payload = json.loads(base64.b64decode(payload_b64))
                    return payload.get('sub') or payload.get('azp', 'anonymous')
                except Exception:
                    pass
        return "anonymous"

    async def Summarize(
        self,
        request: summary_pb2.SummarizeRequest,
        context: grpc.aio.ServicerContext,
    ) -> summary_pb2.SummarizeResponse:
        try:
            user_id = self._get_user_id_from_metadata(context)
            note = Note(note_id=request.note_id, text=request.text)
            result = await self._use_case._summarizer.summarize(note, user_id=user_id)

            return summary_pb2.SummarizeResponse(
                note_id=result.note_id,
                summary=result.summary,
            )
        except NoteTextEmptyError as e:
            await context.abort(grpc.StatusCode.INVALID_ARGUMENT, str(e))
        except SummarizationFailedError as e:
            await context.abort(grpc.StatusCode.INTERNAL, str(e))