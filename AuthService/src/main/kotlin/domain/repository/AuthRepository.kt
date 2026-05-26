package ru.sagenotes.authservice.domain.repository

import ru.sagenotes.authservice.domain.model.response.TokenResponse

interface AuthRepository {
    suspend fun login(username: String, password: String): TokenResponse
    suspend fun refreshToken(refreshToken: String): TokenResponse
    suspend fun logout(refreshToken: String)
}