package ru.sagenotes.searchservice.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.sagenotes.searchservice.domain.model.SearchSource

@Serializable
data class SearchResultDto(
    @SerialName("note_id") val noteId: String,
    @SerialName("text") val text: String,
    @SerialName("score") val score: Double,
    @SerialName("source") val source: SearchSource
)