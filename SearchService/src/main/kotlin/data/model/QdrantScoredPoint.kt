package ru.sagenotes.searchservice.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class QdrantScoredPoint(
    @SerialName("id")
    val id: String,
    @SerialName("score")
    val score: Double,
    @SerialName("payload")
    val payload: JsonObject
)
