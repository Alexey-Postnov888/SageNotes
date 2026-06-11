from pathlib import Path
from dotenv import load_dotenv
from pydantic_settings import BaseSettings

env_path = Path(__file__).resolve().parent.parent.parent / ".env"
load_dotenv(env_path)


class Settings(BaseSettings):
    yandex_cloud_api_key: str = ""
    yandex_cloud_folder: str = ""
    yandex_cloud_model: str = "yandexgpt-lite"
    summarize_temperature: float = 0.3
    summarize_max_tokens: int = 500

    rabbitmq_host: str = "localhost"
    rabbitmq_port: int = 5672
    rabbitmq_username: str = "guest"
    rabbitmq_password: str = "guest"
    rabbitmq_virtual_host: str = "/"


    ai_exchange: str = "ai.events"
    summarize_routing_key: str = "summarize.completed"

    keycloak_client_id: str = "admin-cli"
    keycloak_admin: str = ""
    keycloak_admin_password: str = ""

    keycloak_server_url: str = "http://localhost:8080"
    keycloak_realm: str = "master"
    keycloak_audience: str = "account"

    class Config:
        env_file = str(env_path)
        extra = "ignore"


settings = Settings()