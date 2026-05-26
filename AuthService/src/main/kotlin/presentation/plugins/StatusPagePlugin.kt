package ru.sagenotes.authservice.presentation.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import ru.sagenotes.authservice.data.model.exception.CustomExceptions.LoginException
import ru.sagenotes.authservice.data.model.exception.CustomExceptions.RefreshTokenException
import ru.sagenotes.authservice.presentation.model.response.ErrorResponse

fun Application.configureStatusPagePlugin() {
    install(StatusPages) {
        exception<LoginException.LoginUsernameException> { call, cause ->
            val message = cause.message ?: "Username is required"
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(message))
        }
        exception<LoginException.LoginPasswordException> { call, cause ->
            val message = cause.message ?: "Password is required"
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(message))
        }
        exception<RefreshTokenException> { call, cause ->
            val message = cause.message ?: "Refresh token is required"
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(message))
        }
        exception<Exception> { call, cause ->
            val message = cause.message ?: "Unknown error"
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message))
        }
    }
}