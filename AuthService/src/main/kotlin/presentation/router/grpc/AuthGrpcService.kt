package ru.sagenotes.authservice.presentation.router.grpc

import ru.sagenotes.authservice.domain.usecase.LoginUseCase
import ru.sagenotes.authservice.domain.usecase.RefreshTokenUseCase
import ru.sagenotes.authservice.grpc.AuthServiceGrpcKt
import ru.sagenotes.authservice.grpc.LoginRequest
import ru.sagenotes.authservice.grpc.RefreshRequest
import ru.sagenotes.authservice.grpc.TokenResponse
import ru.sagenotes.authservice.presentation.router.grpc.utils.grpcCall

class AuthGrpcService(
    private val loginUseCase: LoginUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
) : AuthServiceGrpcKt.AuthServiceCoroutineImplBase() {
    override suspend fun login(request: LoginRequest): TokenResponse = grpcCall {
        val result = loginUseCase(request.username, request.password)

        TokenResponse.newBuilder()
            .setAccessToken(result.accessToken)
            .setRefreshToken(result.refreshToken)
            .setExpiresIn(result.expiresIn)
            .setRefreshExpiresIn(result.refreshExpiresIn)
            .build()
    }

    override suspend fun refreshToken(request: RefreshRequest): TokenResponse = grpcCall {
        val result = refreshTokenUseCase(request.refreshToken)

        TokenResponse.newBuilder()
            .setAccessToken(result.accessToken)
            .setRefreshToken(result.refreshToken)
            .setExpiresIn(result.expiresIn)
            .setRefreshExpiresIn(result.refreshExpiresIn)
            .build()
    }
}