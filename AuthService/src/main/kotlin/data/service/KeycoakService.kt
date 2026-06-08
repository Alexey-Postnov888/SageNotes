package ru.sagenotes.authservice.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import ru.sagenotes.authservice.data.config.KeycloakConfig
import ru.sagenotes.authservice.data.model.response.TokenResponseDto

interface KeycloakService {
    suspend fun login(username: String, password: String): TokenResponseDto
    suspend fun refreshToken(refreshToken: String): TokenResponseDto
    suspend fun logout(refreshToken: String)
}
class KeycloakServiceImpl(
    private val httpClient: HttpClient,
    private val config: KeycloakConfig
) : KeycloakService {
    override suspend fun login(username: String, password: String): TokenResponseDto {
        val response = httpClient.submitForm(
            url = config.tokenUrl,
            formParameters = Parameters.build {
                append("grant_type", "password")
                append("client_id", config.clientId)
                append("client_secret", config.clientSecret)
                append("username", username)
                append("password", password)
                append("scope", "openid profile email")
            }
        )

        return response.body<TokenResponseDto>()
    }

    override suspend fun refreshToken(refreshToken: String): TokenResponseDto {
        val response = httpClient.submitForm(
            url = config.tokenUrl,
            formParameters = Parameters.build {
                append("grant_type", "refresh_token")
                append("client_id", config.clientId)
                append("client_secret", config.clientSecret)
                append("refresh_token", refreshToken)
            }
        )

        return response.body<TokenResponseDto>()
    }

    override suspend fun logout(refreshToken: String) {
        httpClient.submitForm(
            url = config.logoutUrl,
            formParameters = Parameters.build {
                append("client_id", config.clientId)
                append("client_secret", config.clientSecret)
                append("refresh_token", refreshToken)
            }
        )
    }
}