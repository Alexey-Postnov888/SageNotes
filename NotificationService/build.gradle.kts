plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "ru.sagenotes.notificationservice"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.websockets)
    implementation(libs.damirdenisTudor.ktorServerRabbitmq)
    implementation(libs.logback.classic)
    implementation(ktorLibs.client.cio)
    implementation(ktorLibs.client.contentNegotiation)

    implementation(ktorLibs.server.statusPages)

    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)

    implementation("io.github.oshai:kotlin-logging-jvm:8.0.01")

    val koin = "4.1.1"
    implementation("io.insert-koin:koin-ktor:$koin")

    implementation("io.grpc:grpc-netty:1.59.0")
    implementation("io.grpc:grpc-protobuf:1.59.0")
    implementation("io.grpc:grpc-stub:1.59.0")
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")

    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.7.0")

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}
