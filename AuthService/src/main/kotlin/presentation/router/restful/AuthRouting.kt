package ru.sagenotes.authservice.presentation.router.restful

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import ru.sagenotes.authservice.domain.usecase.LoginUseCase
import ru.sagenotes.authservice.domain.usecase.RefreshTokenUseCase
import ru.sagenotes.authservice.presentation.mapper.toPresentation
import ru.sagenotes.authservice.presentation.model.request.LoginRequest
import ru.sagenotes.authservice.presentation.model.request.RefreshTokenRequest

fun Application.configureAuthRouting() {
    val loginUseCase: LoginUseCase by inject()
    val refreshTokenUseCase: RefreshTokenUseCase by inject()

    routing {
        route("/auth") {
            post("/login") {
                val request = call.receive<LoginRequest>()
                val loginResponse = loginUseCase(request.username, request.password).toPresentation()
                call.respond(HttpStatusCode.OK, loginResponse)
            }

            post("/refresh") {
                val request = call.receive<RefreshTokenRequest>()
                val refreshTokenResponse = refreshTokenUseCase(request.refreshToken).toPresentation()
                call.respond(HttpStatusCode.OK, refreshTokenResponse)
            }
        }
    }
}