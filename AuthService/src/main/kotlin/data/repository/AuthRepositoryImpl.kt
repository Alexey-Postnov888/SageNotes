package ru.sagenotes.authservice.data.repository

import ru.sagenotes.authservice.data.mapper.toDomain
import ru.sagenotes.authservice.data.service.KeycloakService
import ru.sagenotes.authservice.domain.model.response.TokenResponse
import ru.sagenotes.authservice.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val service: KeycloakService
): AuthRepository {
    override suspend fun login(
        username: String,
        password: String
    ): TokenResponse =
        service.login(username, password).toDomain()

    override suspend fun refreshToken(refreshToken: String): TokenResponse =
        service.refreshToken(refreshToken).toDomain()

    override suspend fun logout(refreshToken: String) {
        service.logout(refreshToken)
    }
}