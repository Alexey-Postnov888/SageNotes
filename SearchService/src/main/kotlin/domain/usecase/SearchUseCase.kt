package ru.sagenotes.searchservice.domain.usecase

import ru.sagenotes.searchservice.domain.model.SearchResult
import ru.sagenotes.searchservice.domain.repository.SearchRepository

interface SearchUseCase {
    suspend operator fun invoke(query: String, userId: String, limit: Int): List<SearchResult>
}

class SearchUseCaseImpl(
    private val repository: SearchRepository
) : SearchUseCase {
    override suspend fun invoke(
        query: String,
        userId: String,
        limit: Int
    ): List<SearchResult> = repository.search(query, userId, limit)
}