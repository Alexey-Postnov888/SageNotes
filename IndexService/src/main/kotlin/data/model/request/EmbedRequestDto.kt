package ru.sagenotes.indexservice.data.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmbedRequestDto(
    @SerialName("texts")
    val texts: List<String>,
)
