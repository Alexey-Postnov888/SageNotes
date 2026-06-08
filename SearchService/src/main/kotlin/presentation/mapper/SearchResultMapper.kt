package ru.sagenotes.searchservice.presentation.mapper

import ru.sagenotes.searchservice.domain.model.SearchResult
import ru.sagenotes.searchservice.presentation.model.SearchResultPresentation

fun SearchResult.toPresentation() = SearchResultPresentation(
    noteId = noteId,
    text = text,
    score = score,
    source = source
)