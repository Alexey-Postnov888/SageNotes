package ru.sagenotes.notificationservice

import io.ktor.server.application.*
import io.ktor.server.websocket.*
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import ru.sagenotes.notificationservice.data.di.appModule
import ru.sagenotes.notificationservice.presentation.consumer.RabbitMqConsumer
import ru.sagenotes.notificationservice.presentation.route.resfull.configureNotificationRouting
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(WebSockets) {
        pingPeriod = 30.seconds
        timeout = 15.seconds
    }

    install(Koin) {
        modules(
            appModule
        )
    }

    configureSerialization()
    configureSecurity()

    configureNotificationRouting()

    val rabbitMqConsumer: RabbitMqConsumer by inject()
    rabbitMqConsumer.start()
}
