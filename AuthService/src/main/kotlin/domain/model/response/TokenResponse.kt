package ru.sagenotes.authservice.domain.model.response

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val refreshExpiresIn: Int
)