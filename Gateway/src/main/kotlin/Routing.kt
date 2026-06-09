package ru.sagenotes.gateway

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val client = HttpClient(CIO)
    routing {
        post("/api/auth/login") {
            try {
                val body = call.receiveText()
                val resp: HttpResponse = client.post("http://auth-service:8080/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }

                call.respondText(resp.bodyAsText(), ContentType.Application.Json, resp.status)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Unknown error")
            }
        }

        post("/api/auth/refresh") {
            try {
                val body = call.receiveText()
                val resp: HttpResponse = client.post("http://auth-service:8080/auth/refresh") {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }

                call.respondText(resp.bodyAsText(), ContentType.Application.Json, resp.status)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Unknown error")
            }
        }

        post("/api/notes") {
            try {
                val body = call.receiveText()
                val resp: HttpResponse = client.post("http://content-service:8080/notes") {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }

                call.respondText(resp.bodyAsText(), ContentType.Application.Json, resp.status)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Unknown error")
            }
        }

        post("/api/summary") {
            try {
                val body = call.receiveText()
                val resp: HttpResponse = client.post("http://ai-service:8080/summary") {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }

                call.respondText(resp.bodyAsText(), ContentType.Application.Json, resp.status)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Unknown error")
            }
        }

        post("api/search") {
            try {
                val query = call.request.queryParameters["q"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Query parameter 'q' is required")
                val limit = call.request.queryParameters["limit"]?.toInt() ?: 10

                val authHeader = call.request.header(HttpHeaders.Authorization)

                val response = client.post("http://search-service:8080/search") {
                    authHeader?.let { header(HttpHeaders.Authorization, it) }

                    url {
                        parameters.append("q", query)
                        parameters.append("limit", limit.toString())
                    }
                }

                call.respondText(response.bodyAsText(), ContentType.Application.Json, response.status)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Unknown error")
            }
        }
    }
}