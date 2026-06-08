package ru.sagenotes.searchservice.data.di

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.sagenotes.searchservice.data.config.ElasticsearchConfig
import ru.sagenotes.searchservice.data.config.JwtConfig
import ru.sagenotes.searchservice.data.config.QdrantConfig
import ru.sagenotes.searchservice.data.repository.SearchRepositoryImpl
import ru.sagenotes.searchservice.data.service.ElasticsearchService
import ru.sagenotes.searchservice.data.service.ElasticsearchServiceImpl
import ru.sagenotes.searchservice.data.service.QdrantService
import ru.sagenotes.searchservice.data.service.QdrantServiceImpl
import ru.sagenotes.searchservice.domain.repository.SearchRepository
import ru.sagenotes.searchservice.domain.usecase.SearchUseCase
import ru.sagenotes.searchservice.domain.usecase.SearchUseCaseImpl

val networkModule = module {
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
}

val configModule = module {
    single { JwtConfig.fromEnv() }
    single { ElasticsearchConfig.fromEnv() }
    single { QdrantConfig.fromEnv() }
}

val serviceModule = module {
    single {
        val config = get<ElasticsearchConfig>()

        val credentials = UsernamePasswordCredentials(config.username, config.password)
        val credentialsProvider = BasicCredentialsProvider().apply {
            setCredentials(AuthScope.ANY, credentials)
        }

        val restClient = RestClient.builder(HttpHost(
            config.host,
            config.port,
            config.scheme
        ))
            .setHttpClientConfigCallback { httpAsyncClientBuilder ->
                httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            }
            .build()

        val transport = RestClientTransport(restClient, JacksonJsonpMapper())
        ElasticsearchClient(transport)
    }

    singleOf(::ElasticsearchServiceImpl) bind ElasticsearchService::class
    singleOf(::QdrantServiceImpl) bind QdrantService::class
}

val repositoryModule = module {
    singleOf(::SearchRepositoryImpl) bind SearchRepository::class
}

val useCaseModule = module {
    singleOf(::SearchUseCaseImpl) bind SearchUseCase::class
}

val appModule = module {
    includes(
        networkModule,
        configModule,
        serviceModule,
        repositoryModule,
        useCaseModule
    )
}