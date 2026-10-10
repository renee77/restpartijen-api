package com.restpartijen.api

import com.restpartijen.api.config.databaseSettings
import com.restpartijen.api.config.jwtProperties
import com.restpartijen.api.persistence.DatabaseFactory
import com.restpartijen.api.plugins.configureCallLogging
import com.restpartijen.api.plugins.configureCors
import com.restpartijen.api.plugins.configureDependencies
import com.restpartijen.api.plugins.configureRequestValidation
import com.restpartijen.api.plugins.configureSerialization
import com.restpartijen.api.plugins.configureStatusPages
import com.restpartijen.api.product.repository.ProductsTable
import com.restpartijen.api.product.routes.configureProductRouting
import com.restpartijen.api.security.repository.UsersTable
import com.restpartijen.api.security.routes.configureAuthRouting
import com.restpartijen.api.security.service.JwtConfig
import com.restpartijen.api.security.service.JwtSettings
import com.restpartijen.api.security.service.configureSecurity
import io.ktor.server.application.*
import io.ktor.server.netty.*
import kotlin.time.Clock

/**
 * Entry point of the application.
 * Hands control to Ktor's Netty EngineMain, which reads resources/application.yaml
 * for the port and the module(s) to load.
 */
fun main(args: Array<String>) {
    EngineMain.main(args)
}

/**
 * The single place where the application is assembled.
 * Referenced by name in application.yaml: com.restpartijen.api.ApplicationKt.module
 */
fun Application.module() {
    // 1. CONFIGURATION
    val jwtProperties = environment.config.jwtProperties()
    log.info("JWT config: $jwtProperties")

    DatabaseFactory.init(environment.config.databaseSettings(), ProductsTable, UsersTable)

    // 2. PLUGINS
    configureSerialization()
    configureDependencies()
    configureStatusPages()
    configureRequestValidation()
    configureCallLogging()
    configureCors()

    // 3. SECURITY
    val jwtSettings = JwtSettings(
        secret = jwtProperties.secret,
        issuer = jwtProperties.issuer,
        audience = jwtProperties.audience,
        validity = jwtProperties.validity
    )

    val jwtConfig = JwtConfig(
        settings = jwtSettings,
        clock = Clock.System
    )

    configureSecurity(jwtConfig)

    // 4. ROUTES
    configureProductRouting()
    // TODO: F2 routes
    // TODO: F3 routes
    configureAuthRouting()
}
