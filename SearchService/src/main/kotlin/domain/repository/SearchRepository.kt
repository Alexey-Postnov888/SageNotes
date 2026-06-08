package ru.sagenotes.searchservice.domain.repository

import ru.sagenotes.searchservice.domain.model.SearchResult

interface SearchRepository {
    suspend fun search(query: String, userId: String, limit: Int = 10): List<SearchResult>
}