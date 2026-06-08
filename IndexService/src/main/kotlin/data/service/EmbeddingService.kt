package ru.sagenotes.indexservice.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import ru.sagenotes.indexservice.data.config.QdrantConfig
import ru.sagenotes.indexservice.data.model.request.EmbedRequestDto
import ru.sagenotes.indexservice.data.model.response.EmbeddingsResponseDto

interface EmbeddingService {
    suspend fun embed(noteId: String, userId: String, chunks: List<String>): List<String>
}

class EmbeddingServiceImpl(
    private val httpClient: HttpClient,
    private val config: QdrantConfig
) : EmbeddingService {
    private val qdrantBaseUrl = config.baseUrl

    override suspend fun embed(noteId: String, userId: String, chunks: List<String>): List<String> {
        try {
            val exists = httpClient.get("$qdrantBaseUrl/collections/${config.collection}").status == HttpStatusCode.OK
            if (!exists) {
                val res = httpClient.put("$qdrantBaseUrl/collections/${config.collection}") {
                    contentType(ContentType.Application.Json)
                    setBody(buildJsonObject {
                        putJsonObject("vectors") {
                            put("size", 384)
                            put("distance", "Cosine")
                        }
                    })
                }
            }

            val embeddingsResponse = httpClient.post("http://embedding-service:8080/embed") {
                contentType(ContentType.Application.Json)
                setBody(EmbedRequestDto(chunks))
            }.body<EmbeddingsResponseDto>()

            return embeddingsResponse.embeddings.mapIndexed { index, vector ->
                val mixedIdString = "$noteId-$index"
                val chunkId = java.util.UUID.nameUUIDFromBytes(mixedIdString.toByteArray()).toString()

                httpClient.put("$qdrantBaseUrl/collections/${config.collection}/points") {
                    contentType(ContentType.Application.Json)
                    setBody(buildJsonObject {
                        putJsonArray("points") {
                            add(buildJsonObject {
                                put("id", chunkId)
                                putJsonArray("vector") {
                                    vector.forEach { add(it) }
                                }
                                putJsonObject("payload") {
                                    put("note_id", noteId)
                                    put("user_id", userId)
                                    put("chunk_index", index)
                                    put("text", chunks[index])
                                }
                            })
                        }
                    })
                }

                chunkId
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
}