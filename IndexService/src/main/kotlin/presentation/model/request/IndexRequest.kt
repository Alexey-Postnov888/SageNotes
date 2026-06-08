package ru.sagenotes.indexservice.presentation.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IndexRequest(
    @SerialName("note_id")
    val noteId: String,
    @SerialName("text")
    val text: String
)