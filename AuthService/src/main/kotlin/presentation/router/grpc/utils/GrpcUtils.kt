package ru.sagenotes.authservice.presentation.router.grpc.utils

import io.grpc.Status
import io.grpc.StatusException
import ru.sagenotes.authservice.data.model.exception.CustomExceptions

suspend fun <T> grpcCall(block: suspend () -> T): T {
    return try {
        block()
    } catch (e: Exception) {
        throw toGrpcException(e)
    }
}

fun toGrpcException(e: Exception): StatusException {
    val status = when (e) {
        is CustomExceptions.LoginException.LoginUsernameException ->
            Status.INVALID_ARGUMENT.withDescription(e.message ?: "Username is required")

        is CustomExceptions.LoginException.LoginPasswordException ->
            Status.INVALID_ARGUMENT.withDescription(e.message ?: "Password is required")

        is CustomExceptions.LoginException ->
            Status.UNAUTHENTICATED.withDescription(e.message ?: "Invalid credentials")

        is CustomExceptions.RefreshTokenException ->
            Status.INVALID_ARGUMENT.withDescription(e.message ?: "Refresh token is required")

        is StatusException -> return e

        else -> Status.INTERNAL.withDescription(e.message ?: "Unknown error")
    }
    return status.asException()
}