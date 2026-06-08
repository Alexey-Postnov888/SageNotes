package ru.sagenotes.searchservice

import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import ru.sagenotes.searchservice.data.di.appModule
import ru.sagenotes.searchservice.presentation.plugins.configureStatusPagePlugin
import ru.sagenotes.searchservice.presentation.route.resful.configureSearchRouting

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
    configureSearchRouting()
}
