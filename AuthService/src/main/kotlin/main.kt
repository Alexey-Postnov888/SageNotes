package ru.sagenotes.authservice

import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.ServerBuilder
import io.ktor.server.application.*
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import ru.sagenotes.authservice.data.di.appModule
import ru.sagenotes.authservice.presentation.plugins.configureStatusPagePlugin
import ru.sagenotes.authservice.presentation.router.grpc.AuthGrpcService
import ru.sagenotes.authservice.presentation.router.restful.configureAuthRouting
import java.io.IOException

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

val logger = KotlinLogging.logger {}

fun Application.module() {
    install(Koin) {
        modules(
            appModule
        )
    }

    configureSerialization()
    configureStatusPagePlugin()
    configureAuthRouting()

    configureGrpcServer()

    configureMetrics()
}

fun Application.configureGrpcServer() {
    val authGrpcService: AuthGrpcService by inject()

    Thread {
        try {
            val grpcServer = ServerBuilder
                .forPort(9090)
                .maxInboundMessageSize(1 * 1024 * 1024)
                .maxInboundMetadataSize(1 * 1024 * 1024)
                .addService(authGrpcService)
                .build()
                .start()

            logger.info { "grpc server started, listening on port 9090" }

            environment.monitor.subscribe(ApplicationStopping) {
                logger.info { "grpc server stopping" }
                grpcServer.shutdown()
                grpcServer.awaitTermination()
                logger.info { "grpc server stopped" }
            }

            grpcServer.awaitTermination()
        } catch (e: IOException) {
            logger.error(e) { "grpc server failed: ${e.message}" }
            e.printStackTrace()
        }
    }.start()

    logger.info { "grpc thread started" }
}
