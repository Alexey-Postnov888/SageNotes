package ru.sagenotes.searchservice.data.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ElasticsearchResultDto(
    val text: String = "",
    @field:JsonProperty("user_id") val userId: String = "",
    @field:JsonProperty("indexed_at") val indexedAt: String = ""
)