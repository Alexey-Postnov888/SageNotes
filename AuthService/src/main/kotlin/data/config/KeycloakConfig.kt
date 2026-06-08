package ru.sagenotes.authservice.data.config

data class KeycloakConfig(
    val url: String,
    val realm: String,
    val clientId: String,
    val clientSecret: String,
) {
    val tokenUrl = "$url/realms/$realm/protocol/openid-connect/token"
    val logoutUrl = "$url/realms/$realm/protocol/openid-connect/logout"

    companion object {
        fun fromEnv() = KeycloakConfig(
            url = requireNotNull(System.getenv("KEYCLOAK_URL")) { "KEYCLOAK_URL is required" },
            realm = requireNotNull(System.getenv("REALM")) { "REALM is required" },
            clientId = requireNotNull(System.getenv("CLIENT_ID")) { "CLIENT_ID is required" },
            clientSecret = requireNotNull(System.getenv("CLIENT_SECRET")) { "CLIENT_SECRET is required" },
        )
    }
}
