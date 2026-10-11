package com.restpartijen.api.reservation

import com.restpartijen.api.plugins.configureSerialization
import com.restpartijen.api.plugins.configureStatusPages
import com.restpartijen.api.reservation.repository.ReservationRepository
import com.restpartijen.api.reservation.routes.configureReservationRouting
import com.restpartijen.api.reservation.service.ReservationService
import com.restpartijen.api.security.TestJwtSettings
import com.restpartijen.api.security.TestTokens
import com.restpartijen.api.security.service.JwtConfig
import com.restpartijen.api.security.service.configureSecurity
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.PriceProvider
import com.restpartijen.api.shared.ProductReader
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductStatusUpdater
import com.restpartijen.api.shared.ProductView
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.FixedClock
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.provide
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Blok 5: POST /api/v1/reservations through all layers, with fakes behind the service.
 * Only COLLECTOR may reserve (§5.5, decision 3.6).
 */
class ReservationRoutesTest {

    // Real "now", because the JWT verifier checks token expiry against the system clock.
    private val now = Clock.System.now()
    private val openWindow = PickupWindow(now - 1.hours, now + 5.hours)
    private val statusUpdater = FakeProductStatusUpdater()

    private fun Application.testApp(products: List<ProductView>) {
        configureSerialization()
        configureStatusPages()
        configureSecurity(JwtConfig(TestJwtSettings.default, Clock.System))
        dependencies {
            provide<ProductReader> { FakeProductReader(products) }
            provide<ProductStatusUpdater> { statusUpdater }
            provide<PriceProvider> { FakePriceProvider(Money(299)) }
            provide<ReservationRepository> { FakeReservationRepository() }
            provide<Clock> { FixedClock(now) }
            provide(::ReservationService)
        }
        configureReservationRouting()
    }

    private fun ApplicationTestBuilder.setUp(vararg products: ProductView) =
        application { testApp(products.toList()) }

    private fun tokenFor(role: Role) =
        TestTokens().createTestToken(userId = 7L, role = role, clock = Clock.System)

    private suspend fun ApplicationTestBuilder.reserve(productId: Long, token: String? = null): HttpResponse =
        client.post("/api/v1/reservations") {
            token?.let { bearerAuth(it) }
            contentType(ContentType.Application.Json)
            setBody("""{"productId": $productId}""")
        }

    @Test
    fun `without a token reserving gives 401`() = testApplication {
        // Arrange
        setUp(FakeProduct(id = 1, pickupWindow = openWindow))
        // Act
        val response = reserve(productId = 1)
        // Assert
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `a supplier cannot reserve and gets 403`() = testApplication {
        // Arrange
        setUp(FakeProduct(id = 1, pickupWindow = openWindow))
        // Act
        val response = reserve(productId = 1, token = tokenFor(Role.SUPPLIER))
        // Assert
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `a collector reserves a listed product and gets 201`() = testApplication {
        // Arrange
        setUp(FakeProduct(id = 1, pickupWindow = openWindow))
        // Act
        val response = reserve(productId = 1, token = tokenFor(Role.COLLECTOR))
        // Assert
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(1L in statusUpdater.reserved, "service did not mark the product reserved")
    }

    @Test
    fun `reserving an already reserved product gives 409`() = testApplication {
        // Arrange
        setUp(FakeProduct(id = 1, pickupWindow = openWindow, status = ProductStatus.RESERVED))
        // Act
        val response = reserve(productId = 1, token = tokenFor(Role.COLLECTOR))
        // Assert
        assertEquals(HttpStatusCode.Conflict, response.status)
    }
}