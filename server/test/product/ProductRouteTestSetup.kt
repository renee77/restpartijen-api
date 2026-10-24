package com.restpartijen.api.product

import com.restpartijen.api.plugins.configureRequestValidation
import com.restpartijen.api.plugins.configureSerialization
import com.restpartijen.api.plugins.configureStatusPages
import com.restpartijen.api.product.client.OpenFoodFactsClient
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.product.repository.SupplierRepository
import com.restpartijen.api.product.routes.configureProductRouting
import com.restpartijen.api.product.service.OpeningHoursService
import com.restpartijen.api.product.service.OwnershipGuard
import com.restpartijen.api.product.service.ProductLookupService
import com.restpartijen.api.product.service.ProductService
import com.restpartijen.api.security.TestJwtSettings
import com.restpartijen.api.security.TestTokens
import com.restpartijen.api.security.service.JwtConfig
import com.restpartijen.api.security.service.configureSecurity
import com.restpartijen.api.shared.PriceProvider
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.FixedClock
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.provide
import kotlin.time.Clock

// Shared by the route tests of F1 (GET, POST, PUT, DELETE of /products, the barcode lookup and the opening
// hours), so the setup and the token rules live in one place.

/** The user id in the tokens; the default fake supplier repository knows [aSupplier] for this user. */
const val SUPPLIER_USER_ID = 7L

/**
 * Starts the F1 routes with fakes behind the service; a database test passes the Exposed repositories.
 *
 * [tokenClock] is fixed during the test (GI-6, decision 6.5), but must start at the real "now":
 * the JWT verifier checks expiry against the real time (#62). Pass the same clock to [productToken].
 */
fun Application.productRoutesTestApp(
    tokenClock: Clock,
    products: ProductRepository = fakeProductRepository(),
    suppliers: SupplierRepository = fakeSupplierRepository(aSupplier, ownerUserId = SUPPLIER_USER_ID),
    openFoodFacts: OpenFoodFactsClient = openFoodFactsNotCalled(),
    priceProvider: PriceProvider = FixedPriceProvider(aBreakdown),
) {
    configureSerialization()
    configureStatusPages()
    configureRequestValidation()
    configureSecurity(JwtConfig(TestJwtSettings.default, tokenClock))
    dependencies {
        provide<ProductRepository> { products }
        provide<SupplierRepository> { suppliers }
        // The listing rules use their own fixed "now", apart from tokenClock: the dates in the bodies build on it.
        provide<Clock> { FixedClock(testNow) }
        provide<PriceProvider> { priceProvider }
        provide(::OwnershipGuard)
        provide(::ProductService)
        provide(::OpeningHoursService)
        provide<OpenFoodFactsClient> { openFoodFacts }
        provide(::ProductLookupService)
    }
    configureProductRouting()
}

/** A token for [userId] with [role], made with the same [tokenClock] as [productRoutesTestApp]. */
fun productToken(role: Role, tokenClock: Clock, userId: Long = SUPPLIER_USER_ID): String =
    TestTokens().createTestToken(userId = userId, role = role, clock = tokenClock)
