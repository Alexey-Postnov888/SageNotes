package ru.sagenotes.indexservice.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmbeddingsResponseDto(
    @SerialName("embeddings")
    val embeddings: List<List<Float>>
)