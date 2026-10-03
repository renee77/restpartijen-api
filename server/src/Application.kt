package com.restpartijen.api

import com.restpartijen.api.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import com.restpartijen.api.persistence.DatabaseFactory
import com.restpartijen.api.product.repository.ExposedProductRepository
import com.restpartijen.api.product.routes.configureProductRouting
import com.restpartijen.api.product.service.ProductService
import com.restpartijen.api.config.databaseSettings
import com.restpartijen.api.config.jwtProperties
import com.restpartijen.api.persistence.DatabaseFactory.init
import com.restpartijen.api.plugins.configureDependencies
import com.restpartijen.api.product.repository.ProductsTable
import com.restpartijen.api.shared.ReservationMaintenance
import io.ktor.server.application.log

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

    init(environment.config.databaseSettings())

    // 2. PLUGINS
    configureSerialization()
    configureDependencies()

    init(environment.config.databaseSettings(), ProductsTable)
    val exposedProductRepository = ExposedProductRepository()
    val productService = ProductService(exposedProductRepository)
    configureProductRouting()
}