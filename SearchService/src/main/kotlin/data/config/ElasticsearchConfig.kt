package ru.sagenotes.searchservice.data.config

data class ElasticsearchConfig(
    val host: String,
    val port: Int = 9200,
    val username: String,
    val password: String,
    val scheme: String = "http",
) {
    companion object {
        fun fromEnv() = ElasticsearchConfig(
            host = requireNotNull(System.getenv("ELASTICSEARCH_HOST")) { "ELASTICSEARCH_HOST is required" },
            username = requireNotNull(System.getenv("ELASTICSEARCH_USERNAME")) { "ELASTICSEARCH_USERNAME is required" },
            password = requireNotNull(System.getenv("ELASTICSEARCH_PASSWORD")) { "ELASTICSEARCH_PASSWORD is required" },
        )
    }
}