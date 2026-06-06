package ru.sagenotes.indexservice.presentation.route.restful

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ru.sagenotes.indexservice.domain.usecase.IndexUseCase
import ru.sagenotes.indexservice.presentation.model.request.IndexRequest

fun Application.configureIndexRouting() {
    val indexUseCase: IndexUseCase by inject()

    routing {
        post("/index") {
            try {
                val request = call.receive<IndexRequest>()
                indexUseCase(
                    noteId = request.noteId,
                    text = request.text,
                    userId = ""
                )
                call.respond(HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    e.message ?: "Unknown error"
                )
            }
        }
    }
}