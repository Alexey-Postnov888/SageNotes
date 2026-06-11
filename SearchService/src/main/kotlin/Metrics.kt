package ru.sagenotes.searchservice

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.config.MeterFilter
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Application.configureMetrics() {
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    registry.config().commonTags(
        listOf(
            Tag.of("service", "search-service")
        )
    )

    registry.config().meterFilter(
        MeterFilter.deny { id ->
            val route = id.getTag("route") ?: id.getTag("uri")
            route == "/metrics"
        }
    )

    install(MicrometerMetrics) {
        this.registry = registry

        distributionStatisticConfig = DistributionStatisticConfig.Builder()
            .percentilesHistogram(true)
            .build()

        timers { call, throwable ->
            val path = call.request.path()
                .replace(Regex("/\\d+"), "/{id}")
                .replace(Regex("/[0-9a-fA-F-]{36}"), "/{uuid}")

            tag("uri", path)
            tag("method", call.request.httpMethod.value)
            tag("status", call.response.status()?.value?.toString() ?: "Unknown")
        }
    }

    routing {
        get("/metrics") {
            call.respondText(registry.scrape(), ContentType.Text.Plain)
        }
    }
}