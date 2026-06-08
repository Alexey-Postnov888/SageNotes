package ru.sagenotes.searchservice.data.mapper

import ru.sagenotes.searchservice.data.model.SearchResultDto
import ru.sagenotes.searchservice.domain.model.SearchResult

fun SearchResultDto.toDomain(): SearchResult = SearchResult(
    noteId = noteId,
    text = text,
    score = score,
    source = source
)