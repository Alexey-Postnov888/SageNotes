package ru.sagenotes.indexservice.data.repository

import ru.sagenotes.indexservice.data.service.ElasticsearchService
import ru.sagenotes.indexservice.data.service.EmbeddingService
import ru.sagenotes.indexservice.data.utils.Chunker
import ru.sagenotes.indexservice.domain.repository.IndexRepository

class IndexRepositoryImpl(
    private val elasticsearchService: ElasticsearchService,
    private val embeddingService: EmbeddingService,
    private val chunker: Chunker
): IndexRepository {
    override suspend fun index(noteId: String, text: String, userId: String) {
        try {
            val chunks = chunker.chunk(text)
            embeddingService.embed(
                noteId = noteId,
                chunks = chunks,
                userId = userId,
            )
            elasticsearchService.index(noteId, text, userId)
        } catch (e: Exception) {
            println(e)
        }
    }
}