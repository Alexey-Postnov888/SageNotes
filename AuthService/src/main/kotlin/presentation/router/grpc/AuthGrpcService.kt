package ru.sagenotes.authservice.presentation.router.grpc

import ru.sagenotes.authservice.domain.usecase.LoginUseCase
import ru.sagenotes.authservice.domain.usecase.RefreshTokenUseCase
import ru.sagenotes.authservice.grpc.AuthServiceGrpcKt
import ru.sagenotes.authservice.grpc.LoginRequest
import ru.sagenotes.authservice.grpc.RefreshRequest
import ru.sagenotes.authservice.grpc.TokenResponse

class AuthGrpcService(
    private val loginUseCase: LoginUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
) : AuthServiceGrpcKt.AuthServiceCoroutineImplBase() {
    override suspend fun login(request: LoginRequest): TokenResponse {
        val result = loginUseCase(request.username, request.password)

        return TokenResponse.newBuilder()
            .setAccessToken(result.accessToken)
            .setRefreshToken(result.refreshToken)
            .setExpiresIn(result.expiresIn)
            .setRefreshExpiresIn(result.refreshExpiresIn)
            .build()
    }

    override suspend fun refreshToken(request: RefreshRequest): TokenResponse {
        val result = refreshTokenUseCase(request.refreshToken)

        return TokenResponse.newBuilder()
            .setAccessToken(result.accessToken)
            .setRefreshToken(result.refreshToken)
            .setExpiresIn(result.expiresIn)
            .setRefreshExpiresIn(result.refreshExpiresIn)
            .build()
    }
}