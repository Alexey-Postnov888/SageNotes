package ru.sagenotes.authservice.data.mapper

import ru.sagenotes.authservice.data.model.response.TokenResponseDto
import ru.sagenotes.authservice.domain.model.response.TokenResponse

fun TokenResponseDto.toDomain(): TokenResponse = TokenResponse(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresIn = expiresIn,
    refreshExpiresIn = refreshExpiresIn
)