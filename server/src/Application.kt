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
import com.restpartijen.api.product.repository.ProductAllergensTable
import com.restpartijen.api.pricing.configureMaintenanceLoop
import com.restpartijen.api.pricing.routes.configurePricingRouting
import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.product.routes.configureProductRouting
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.security.routes.configureAuthRouting
import com.restpartijen.api.security.configureSecurityModule
import com.restpartijen.api.security.service.JwtSettings
import com.restpartijen.api.reservation.repository.ReservationsTable
import com.restpartijen.api.security.service.PasswordHasher
import com.restpartijen.api.seed.SeedData
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
 * The single place where the application is assembled (GI-2, decision 2.2).
 * Referenced by name in application.yaml: com.restpartijen.api.ApplicationKt.module
 */
fun Application.module() {
    // 1. CONFIGURATION
    val jwtProperties = environment.config.jwtProperties()
    log.info("JWT config: $jwtProperties")

    //Zet een tabel na de tabel waar hij naar verwijst.
    DatabaseFactory.init(
        environment.config.databaseSettings(),
        UsersTable,
        SuppliersTable,
        ProductsTable,
        ProductAllergensTable,
        ReservationsTable
    )

    SeedData.seedIfEmpty(clock = Clock.System, hasher = PasswordHasher())

    // 2. PLUGINS
    configureSerialization()
    configureDependencies()
    configureMaintenanceLoop()
    configureStatusPages()
    configureRequestValidation()
    configureCallLogging()
    configureCors()

    // 3. SECURITY
    configureSecurityModule(
        JwtSettings(
            secret = jwtProperties.secret,
            issuer = jwtProperties.issuer,
            audience = jwtProperties.audience,
            validity = jwtProperties.validity,
        )
    )

    // 4. ROUTES
    configureProductRouting()
    // TODO: F2 routes
//    configureReservationRouting()
    // TODO: F3 routes
    configureAuthRouting()
    // F3, owner Lonneke: admin overview and status monitoring (US-08, US-09).
    configurePricingRouting()
}
