package ru.sagenotes.indexservice.presentation.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import ru.sagenotes.indexservice.presentation.model.response.ErrorResponse

fun Application.configureStatusPagePlugin() {
    install(StatusPages) {
        exception<Exception> { call, cause ->
            val message = cause.message ?: "Unknown error"
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message))
        }
    }
}