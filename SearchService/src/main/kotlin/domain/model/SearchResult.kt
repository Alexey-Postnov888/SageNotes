package ru.sagenotes.searchservice.domain.model

data class SearchResult(
   val noteId: String,
   val text: String,
   val score: Double,
   val source: SearchSource
)