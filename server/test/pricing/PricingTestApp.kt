package com.restpartijen.api.pricing

import com.restpartijen.api.plugins.configureSerialization
import com.restpartijen.api.pricing.routes.configurePricingRouting
import com.restpartijen.api.pricing.service.ExpiryScheduler
import com.restpartijen.api.security.TestClock
import com.restpartijen.api.security.configureStatusPages
import com.restpartijen.api.security.jwtConfig
import com.restpartijen.api.security.service.configureSecurity
import com.restpartijen.api.shared.ProductReader
import com.restpartijen.api.shared.ProductStatusUpdater
import com.restpartijen.api.shared.ReservationMaintenance
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

/**
 * Test app for the pricing endpoints: real route, fakes behind the service.
 * Same pieces as setUpTestApp() in the security tests, plus serialization and DI.
 */
fun Application.setUpPricingTestApp(
    // The fakes are passed in so that the test can control their behavior.
    reservationMaintenance: ReservationMaintenance = FakeReservationMaintenance(),
    productReader: ProductReader = FakeProductReader(),
    productStatusUpdater: ProductStatusUpdater = FakeProductStatusUpdater(),
) {
    // temporary: replace with Stefan's StatusPages (GI-4) when it is on main
    configureStatusPages()
    // turns the MaintenanceReport into JSON
    configureSerialization()
    // creates the JWT provider that requireRole needs
    configureSecurity(jwtConfig)

    // The route requires a DI binding for ExpiryScheduler, which is the service that runs the maintenance. We provide a fake implementation for testing.
    dependencies.provide<ExpiryScheduler> {
        ExpiryScheduler(reservationMaintenance, productReader, productStatusUpdater, TestClock.fixed)
    }

    // The real route from src/pricing/routes/PricingRoutes.kt, which uses the ExpiryScheduler service. In this test app, the service is a fake that we control.
    configurePricingRouting()
}