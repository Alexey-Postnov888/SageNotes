package ru.sagenotes.indexservice.data.config

data class QdrantConfig(
    private val host: String,
    private val port: Int = 6333,
) {
    val baseUrl = "http://$host:$port"

    companion object {
        fun fromEnv() = QdrantConfig(
            host = System.getenv("QDRANT_HOST")
        )
    }
}