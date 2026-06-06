package ru.sagenotes.indexservice.data.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import java.time.Instant

interface ElasticsearchService {
    suspend fun index(noteId: String, text: String, userId: String)
}

class ElasticsearchServiceImpl(
    private val elasticsearchClient: ElasticsearchClient
) : ElasticsearchService {
    override suspend fun index(noteId: String, text: String, userId: String) {
        try {
            elasticsearchClient.index {
                it.index("notes")
                    .id(noteId)
                    .document(mapOf(
                        "text" to text,
                        "userId" to userId,
                        "indexed_at" to Instant.now().toString()
                    ))
            }
        } catch (e: Exception) {
            println(e)
        }
    }
}