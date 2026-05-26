package ru.sagenotes.authservice.presentation.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenResponsePresentation(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("refresh_expires_in")
    val refreshExpiresIn: Int
)
