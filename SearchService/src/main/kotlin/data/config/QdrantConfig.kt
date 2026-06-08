package ru.sagenotes.searchservice.data.config

data class QdrantConfig(
    private val host: String,
    private val port: Int = 6333,
    val collection: String
) {
    val baseUrl = "http://$host:$port"

    companion object {
        fun fromEnv() = QdrantConfig(
            host = requireNotNull(System.getenv("QDRANT_HOST")) { "QDRANT_HOST is required" },
            collection = requireNotNull(System.getenv("QDRANT_COLLECTION")) { "QDRANT_COLLECTION is required" },
        )
    }
}