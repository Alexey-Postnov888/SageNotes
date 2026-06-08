package ru.sagenotes.searchservice.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QdrantSearchResponse(
    @SerialName("result")
    val result: List<QdrantScoredPoint>
)
