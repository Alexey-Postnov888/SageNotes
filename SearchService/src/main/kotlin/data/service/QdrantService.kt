package ru.sagenotes.searchservice.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import ru.sagenotes.searchservice.data.config.QdrantConfig
import ru.sagenotes.searchservice.data.model.*
import ru.sagenotes.searchservice.domain.model.SearchSource

interface QdrantService {
    suspend fun search(query: String, userId: String, limit: Int = 10): List<SearchResultDto>
}

class QdrantServiceImpl(
    private val httpClient: HttpClient,
    private val config: QdrantConfig
) : QdrantService {
    override suspend fun search(
        query: String,
        userId: String,
        limit: Int
    ): List<SearchResultDto> {
        return withContext(Dispatchers.IO) {
            val embeddingsResponse = httpClient.post("http://embedding-service:8080/embed") {
                contentType(ContentType.Application.Json)
                setBody(EmbedRequestDto(listOf(query)))
            }.body<EmbeddingsResponseDto>()

            val queryVector = embeddingsResponse.embeddings.firstOrNull()
                ?: return@withContext emptyList()

            val searchBodyRequest = buildJsonObject {
                putJsonArray("vector") {
                    queryVector.forEach { add(it) }
                }
                put("limit", limit)
                put("with_payload", true)

                putJsonObject("filter") {
                    putJsonArray("must") {
                        addJsonObject {
                            put("key", "user_id")
                            putJsonObject("match") {
                                put("value", userId)
                            }
                        }
                    }
                }
            }

            val response = httpClient.post("${config.baseUrl}/collections/${config.collection}/points/search") {
                contentType(ContentType.Application.Json)
                setBody(searchBodyRequest)
            }.body<QdrantSearchResponse>()

            response.result.map { point ->
                SearchResultDto(
                    noteId = point.payload["note_id"]?.jsonPrimitive?.content ?: "",
                    text = point.payload["text"]?.jsonPrimitive?.content ?: "",
                    score = point.score,
                    source = SearchSource.QDRANT
                )
            }
        }
    }
}