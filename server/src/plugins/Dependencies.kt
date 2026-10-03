package com.restpartijen.api.plugins

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.provide
// TODO: imports for the product repository types and ProductService
import com.restpartijen.api.product.repository.ExposedProductRepository
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.product.service.ProductService


/**
 * The one place that says which implementation belongs to which contract (§12.1).
 * Tests can register a fake before module() runs; the test engine then ignores this registration.
 */
fun Application.configureDependencies() {
    dependencies {
        // F1
        provide<ProductRepository>(::ExposedProductRepository)
        provide(::ProductService)

        // F2 (Stefan), F3 (Lonneke): register their contracts here once they exist


    }
}