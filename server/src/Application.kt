package com.restpartijen.api

import com.restpartijen.api.config.databaseSettings
import com.restpartijen.api.config.jwtProperties
import com.restpartijen.api.persistence.DatabaseFactory
import com.restpartijen.api.plugins.configureCallLogging
import com.restpartijen.api.plugins.configureCors
import com.restpartijen.api.plugins.configureDependencies
import com.restpartijen.api.plugins.configureRequestValidation
import com.restpartijen.api.plugins.configureSerialization
import com.restpartijen.api.product.routes.configureProductRouting
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.netty.EngineMain
import com.restpartijen.api.plugins.configureStatusPages
import com.restpartijen.api.product.repository.ProductsTable
import com.restpartijen.api.product.routes.configureProductRouting
import io.ktor.server.application.*
import io.ktor.server.netty.*

/**
 * Entry point of the application.
 * Hands control to Ktor's Netty EngineMain, which reads resources/application.yaml
 * for the port and the module(s) to load.
 */
fun main(args: Array<String>) {
    EngineMain.main(args)
}

/**
 * The single place where the application is assembled (GI-2, decision 2.2).
 * Referenced by name in application.yaml: com.restpartijen.api.ApplicationKt.module
 */
fun Application.module() {
    // 1. CONFIGURATION
    val jwtProperties = environment.config.jwtProperties()
    log.info("JWT config: $jwtProperties")

    DatabaseFactory.init(environment.config.databaseSettings(), ProductsTable)

    // 2. PLUGINS
    configureCallLogging()
    configureCors()
    configureSerialization()
    configureDependencies()
    configureStatusPages()
    configureRequestValidation()

    // 3. SECURITY
    // TODO: map jwtProperties to JwtSettings and configureSecurity()

    // 4. ROUTES
    configureProductRouting()
    // TODO: F2 routes
    // TODO: F3 routes
}
