package ru.sagenotes.authservice.presentation.mapper

import ru.sagenotes.authservice.domain.model.response.TokenResponse
import ru.sagenotes.authservice.presentation.model.response.TokenResponsePresentation

fun TokenResponse.toPresentation(): TokenResponsePresentation = TokenResponsePresentation(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresIn = expiresIn,
    refreshExpiresIn = refreshExpiresIn
)