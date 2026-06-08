package ru.sagenotes.authservice.data.di

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.sagenotes.authservice.data.config.KeycloakConfig
import ru.sagenotes.authservice.data.repository.AuthRepositoryImpl
import ru.sagenotes.authservice.data.service.KeycloakService
import ru.sagenotes.authservice.data.service.KeycloakServiceImpl
import ru.sagenotes.authservice.domain.repository.AuthRepository
import ru.sagenotes.authservice.domain.usecase.LoginUseCase
import ru.sagenotes.authservice.domain.usecase.LoginUseCaseImpl
import ru.sagenotes.authservice.domain.usecase.RefreshTokenUseCase
import ru.sagenotes.authservice.domain.usecase.RefreshTokenUseCaseImpl
import ru.sagenotes.authservice.presentation.router.grpc.AuthGrpcService

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
    single { KeycloakConfig.fromEnv() }
}

val serviceModule = module {
    singleOf(::KeycloakServiceImpl) bind KeycloakService::class
}

val repositoryModule = module {
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
}

val useCaseModule = module {
    singleOf(::LoginUseCaseImpl) bind LoginUseCase::class
    singleOf(::RefreshTokenUseCaseImpl) bind RefreshTokenUseCase::class
}

val grpcModule = module {
    singleOf(::AuthGrpcService)
}

val appModule = module {
    includes(
        networkModule,
        configModule,
        serviceModule,
        repositoryModule,
        useCaseModule,
        grpcModule
    )
}