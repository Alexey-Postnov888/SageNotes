from app.config import settings


class KeycloakSettings:
    def __init__(self):
        self.jwks_url = f"{settings.keycloak_server_url}/realms/{settings.keycloak_realm}/protocol/openid-connect/certs"
        self.issuer = f"{settings.keycloak_server_url}/realms/{settings.keycloak_realm}"
        self.issuer_fallback = f"http://localhost:8080/realms/{settings.keycloak_realm}"
        self.KEYCLOAK_AUDIENCE = settings.keycloak_audience


settingKeycloak = KeycloakSettings()