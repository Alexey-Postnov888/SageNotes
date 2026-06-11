import asyncio
import grpc
from app.proto import summary_pb2, summary_pb2_grpc


async def main():
    # Токен (получи через Swagger /auth/token)
    token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJfQWFESDJGN1BpV2R2SjZieTVfVTFHMklhWnNIN2d4dzdoQkdrWC01REF3In0.eyJleHAiOjE3ODExNTk4NDYsImlhdCI6MTc4MTE1OTc4NiwianRpIjoiMjdlNTQ3NGUtM2Y2Ni00YWJjLWJjZjktZmUxMTBlZTMwYWQzIiwiaXNzIjoiaHR0cDovL2tleWNsb2FrOjgwODAvcmVhbG1zL21hc3RlciIsInR5cCI6IkJlYXJlciIsImF6cCI6ImFkbWluLWNsaSIsInNpZCI6IjY4ZmQ4OWU2LWEyNTQtNDM5MC1iODZiLTNjY2ViMWZmZjlkMSIsInNjb3BlIjoiZW1haWwgcHJvZmlsZSJ9.tlfUTY1RJGeD7fzdTpjHNhH4WByHKC6pJNNLkpMEiz6DXkl7eOhwcSIyQ5YUHS18bcVwTS6iUe0tNozIWi0UJDIIBeN4v7g3YpuxNgJEf6aQfB9dHBF-neFkjhB-L8i29oW-xGKB3qkcTFYFh3BLo6AiKBcTmgG3XMkcjiP9ZZs-0zeRW6OiiM9xPdsrYo_02gOvvUPPzzrz7U9m_XrSsrjzwFPW6D98eR2-MwoxTkttkW2VyGq0vyVzqjJ2L-eS1ihVLXtoH8m3RrG5VRhCwFfAmcxtzQ_G2VaPOZRqmi_2ycA2r9WAkxNuYsgP12tXTfrsYvx0nUd3c91GVWPjrg"
    
    # Метаданные с авторизацией
    metadata = [
        ("authorization", f"Bearer {token}"),
    ]
    
    async with grpc.aio.insecure_channel("localhost:9090") as channel:
        stub = summary_pb2_grpc.SummaryServiceStub(channel)
        response = await stub.Summarize(
            summary_pb2.SummarizeRequest(
                note_id="grpc-test",
                text="Docker — это платформа для контейнеризации приложений.",
            ),
            metadata=metadata,
        )
        print(f"Note ID: {response.note_id}")
        print(f"Summary: {response.summary}")


asyncio.run(main())