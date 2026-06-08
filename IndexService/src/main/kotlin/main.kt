package ru.sagenotes.indexservice

import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import ru.sagenotes.indexservice.data.di.appModule
import ru.sagenotes.indexservice.presentation.plugins.configureStatusPagePlugin
import ru.sagenotes.indexservice.presentation.route.restful.configureIndexRouting

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(Koin) {
        modules(
            appModule
        )
    }

    configureSerialization()
    configureStatusPagePlugin()
    configureSecurity()
    configureIndexRouting()
}
