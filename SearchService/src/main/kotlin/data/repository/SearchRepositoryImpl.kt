package ru.sagenotes.searchservice.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import ru.sagenotes.searchservice.data.mapper.toDomain
import ru.sagenotes.searchservice.data.model.SearchResultDto
import ru.sagenotes.searchservice.data.service.ElasticsearchService
import ru.sagenotes.searchservice.data.service.QdrantService
import ru.sagenotes.searchservice.domain.model.SearchResult
import ru.sagenotes.searchservice.domain.model.SearchSource
import ru.sagenotes.searchservice.domain.repository.SearchRepository

class SearchRepositoryImpl(
    private val elasticsearchService: ElasticsearchService,
    private val qdrantService: QdrantService
) : SearchRepository {
    private val rrfK = 60

    override suspend fun search(
        query: String,
        userId: String,
        limit: Int
    ): List<SearchResult> {
        return withContext(Dispatchers.Default) {
            val elasticsearchDeferred = async { elasticsearchService.search(query, userId, limit * 2) }
            val qdrantDeferred = async { qdrantService.search(query, userId, limit * 2) }

            val elasticsearchResults = runCatching { elasticsearchDeferred.await() }.getOrDefault(emptyList())
            val rawQdrantResults = runCatching { qdrantDeferred.await() }.getOrDefault(emptyList())

            val uniqueQdrantResults = rawQdrantResults
                .groupBy { it.noteId }
                .map { (_, chunks) ->
                    chunks.maxByOrNull { it.score }!!
                }

            val rrfScores = mutableMapOf<String, Double>()
            val docsMap = mutableMapOf<String, SearchResultDto>()

            elasticsearchResults.forEachIndexed { index, dto ->
                val rank = index + 1
                val score = 1.0 / (rrfK + rank)
                rrfScores[dto.noteId] = rrfScores.getOrDefault(dto.noteId, 0.0) + score
                docsMap[dto.noteId] = dto
            }

            uniqueQdrantResults.forEachIndexed { index, dto ->
                val rank = index + 1
                val score = 1.0 / (rrfK + rank)
                rrfScores[dto.noteId] = rrfScores.getOrDefault(dto.noteId, 0.0) + score

                val existingDto = docsMap[dto.noteId]
                if (existingDto != null) {
                    docsMap[dto.noteId] = existingDto.copy(
                        source = SearchSource.HYBRID,
                        text = "${existingDto.text} \n[Семантика]: ${dto.text}"
                    )
                } else {
                    docsMap[dto.noteId] = dto
                }
            }

            rrfScores.entries
                .sortedByDescending { it.value }
                .take(limit)
                .mapNotNull { entry ->
                    val dto = docsMap[entry.key] ?: return@mapNotNull null
                    dto.copy(score = entry.value).toDomain()
                }
        }
    }
}