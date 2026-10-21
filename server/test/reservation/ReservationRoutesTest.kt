package com.restpartijen.api.reservation

import com.restpartijen.api.plugins.configureSerialization
import com.restpartijen.api.plugins.configureStatusPages
import com.restpartijen.api.reservation.dto.ReservationResponse
import com.restpartijen.api.reservation.model.Reservation
import com.restpartijen.api.reservation.model.ReservationStatus
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
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.provide
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
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

    private fun Application.testApp(products: List<ProductView>, reservations: List<Reservation> = emptyList(), price: Money? = Money.fromCents(299)) {
        createEmptyTestDatabase()     // The service opens its own transaction (GI-1, decision 1.4), so even with fakes it needs a database.
        configureSerialization()
        configureStatusPages()
        configureSecurity(JwtConfig(TestJwtSettings.default, Clock.System))
        dependencies {
            provide<ProductReader> { FakeProductReader(products) }
            provide<ProductStatusUpdater> { statusUpdater }
            provide<PriceProvider> { FakePriceProvider(price) }
            provide<ReservationRepository> { FakeReservationRepository(reservations) }
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

    private suspend fun ApplicationTestBuilder.ownReservations(token: String? = null): HttpResponse =
        client.get("/api/v1/reservations") { token?.let { bearerAuth(it) } }

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
    fun `a collector reserves a listed product and gets 201 with the price in euros`() = testApplication {
        // Arrange
        setUp(FakeProduct(id = 1, pickupWindow = openWindow))
        // Act
        val response = reserve(productId = 1, token = tokenFor(Role.COLLECTOR))
        // Assert
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(1L in statusUpdater.reserved, "service did not mark the product reserved")
        val body = Json.decodeFromString<ReservationResponse>(response.bodyAsText())
        assertEquals(2.99, body.reservedPrice)
        assertEquals(ReservationStatus.ACTIVE, body.status)
        assertEquals(1L, body.productId)
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

    @Test
    fun `without a token the own reservations give 401`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = ownReservations()
        // Assert
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `a supplier asking for own reservations gets 403`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = ownReservations(token = tokenFor(Role.SUPPLIER))
        // Assert
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `a collector only sees their own reservations`() = testApplication {
        // Arrange: one reservation of collector 7 (the token), one of collector 8
        val mine = Reservation(
            1, productId = 1, collectorId = 7, reservedPrice = Money.fromCents(299),
            reservedAt = now - 1.hours, expiresAt = now + 5.hours,
        )
        val theirs = mine.copy(id = 2, productId = 2, collectorId = 8)
        application { testApp(products = emptyList(), reservations = listOf(mine, theirs)) }

        // Act
        val response = ownReservations(token = tokenFor(Role.COLLECTOR))

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.decodeFromString<List<ReservationResponse>>(response.bodyAsText())
        assertEquals(listOf(1L), body.map { it.id })
    }

    @Test
    fun `the reserved price stays the same when the current price drops`() = testApplication {
        // Arrange: reserved at 4.50 twenty hours ago; the price provider now says 2.99
        val reservation = Reservation(
            1, productId = 1, collectorId = 7, reservedPrice = Money.fromCents(450),
            reservedAt = now - 20.hours, expiresAt = now + 4.hours,
        )
        application {
            testApp(products = listOf(FakeProduct(id = 1, pickupWindow = openWindow)), reservations = listOf(reservation))
        }

        // Act
        val response = ownReservations(token = tokenFor(Role.COLLECTOR))

        // Assert: the stored price, not the current one (§5.5)
        val body = Json.decodeFromString<List<ReservationResponse>>(response.bodyAsText())
        assertEquals(4.5, body.single().reservedPrice)
    }

    @Test
    fun `reserving an unknown product gives 404`() = testApplication {
        // Arrange: no products at all
        setUp()
        // Act
        val response = reserve(productId = 99, token = tokenFor(Role.COLLECTOR))
        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `reserving a removed product gives 404`() = testApplication {
        // Arrange: for the public API a REMOVED product no longer exists (B-32)
        setUp(FakeProduct(id = 1, pickupWindow = openWindow, status = ProductStatus.REMOVED))
        // Act
        val response = reserve(productId = 1, token = tokenFor(Role.COLLECTOR))
        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `reserving an expired product that is still LISTED gives 409`() = testApplication {
        // Arrange: status says LISTED, but the price provider returns null: past its best-before moment (B-18)
        application { testApp(listOf(FakeProduct(id = 1, pickupWindow = openWindow)), price = null) }
        // Act
        val response = reserve(productId = 1, token = tokenFor(Role.COLLECTOR))
        // Assert: refused, and the product was not touched
        assertEquals(HttpStatusCode.Conflict, response.status)
        assertTrue(statusUpdater.reserved.isEmpty())
    }

    @Test
    fun `reserving after the pickup window gives 422`() = testApplication {
        // Arrange: still sellable, but the window ended an hour ago
        setUp(FakeProduct(id = 1, pickupWindow = PickupWindow(now - 5.hours, now - 1.hours)))
        // Act
        val response = reserve(productId = 1, token = tokenFor(Role.COLLECTOR))
        // Assert
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertTrue(statusUpdater.reserved.isEmpty())
    }

    @Test
    fun `a reservation expires at the end of the pickup window when that comes before 24 hours`() = testApplication {
        // Arrange: the window closes in 5 hours, well before now + 24h
        setUp(FakeProduct(id = 1, pickupWindow = openWindow))
        // Act
        val response = reserve(productId = 1, token = tokenFor(Role.COLLECTOR))
        // Assert (§5.5)
        val body = Json.decodeFromString<ReservationResponse>(response.bodyAsText())
        assertEquals(openWindow.until.toString(), body.expiresAt)
    }
}