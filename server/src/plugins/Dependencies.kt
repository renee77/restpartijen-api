package com.restpartijen.api.plugins

import com.restpartijen.api.pricing.service.AdminProductService
import com.restpartijen.api.pricing.service.PricingService
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.provide
import com.restpartijen.api.product.repository.ExposedProductReader
import com.restpartijen.api.product.repository.ExposedProductRepository
import com.restpartijen.api.product.repository.ExposedProductStatusUpdater
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.product.client.OpenFoodFactsClient
import com.restpartijen.api.product.service.OwnershipGuard
import com.restpartijen.api.product.service.NotFoundCache
import com.restpartijen.api.product.service.OpenFoodFactsRateLimiter
import com.restpartijen.api.product.service.ProductLookupCache
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import com.restpartijen.api.product.service.ProductService
import com.restpartijen.api.shared.PriceProvider
import com.restpartijen.api.shared.ProductStatusUpdater
import com.restpartijen.api.pricing.service.ExpiryScheduler
import com.restpartijen.api.product.repository.ExposedSupplierRepository
import com.restpartijen.api.product.repository.SupplierRepository
import com.restpartijen.api.shared.ProductReader
import com.restpartijen.api.shared.ReservationMaintenance
import com.restpartijen.api.reservation.repository.ExposedReservationRepository
import com.restpartijen.api.reservation.repository.ReservationRepository
import com.restpartijen.api.reservation.service.ReservationMaintenanceService
import com.restpartijen.api.reservation.service.ReservationService
import kotlin.time.Clock


/**
 * The one place that says which implementation belongs to which contract (§12.1).
 * Tests can register a fake before module() runs; the test engine then ignores this registration.
 */
fun Application.configureDependencies() {
    dependencies {
        // Shared
        provide<Clock> { Clock.System }

        // F1
        provide<ProductRepository>(::ExposedProductRepository)
        provide(::OwnershipGuard)
        provide(::ProductService)
        provide<ProductReader>(::ExposedProductReader)
        provide<ProductStatusUpdater>(::ExposedProductStatusUpdater)
        provide<SupplierRepository>(::ExposedSupplierRepository)
        // The real HTTP engine; tests give the client a MockEngine instead (US-02, A5).
        // Registered on its own, so the DI closes it when the application stops; HttpClient leaves it open.
        provide<HttpClientEngine> { CIO.create() }
        provide(::OpenFoodFactsClient)
        provide(::ProductLookupCache)
        provide(::NotFoundCache)
        provide(::OpenFoodFactsRateLimiter)

        // F2 (Stefan)
        provide<ReservationRepository>(::ExposedReservationRepository)
        provide<ReservationMaintenance>(::ReservationMaintenanceService)
        provide(::ReservationService)

        // F3: Pricing
        provide<PriceProvider>(::PricingService)
        provide<ExpiryScheduler>(::ExpiryScheduler)
        provide<AdminProductService>(::AdminProductService)

    }
}