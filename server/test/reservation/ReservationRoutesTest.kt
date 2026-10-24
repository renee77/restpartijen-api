package com.restpartijen.api.reservation

import com.restpartijen.api.plugins.configureSerialization
import com.restpartijen.api.plugins.configureStatusPages
import com.restpartijen.api.reservation.dto.ReservationResponse
import com.restpartijen.api.reservation.model.Reservation
import com.restpartijen.api.reservation.model.ReservationStatus
import com.restpartijen.api.reservation.repository.ReservationRepository
import com.restpartijen.api.reservation.routes.configureReservationRouting
import com.restpartijen.api.reservation.service.ReservationService
import com.restpartijen.api.security.TestClock
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
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
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
 * The reservation endpoints through all layers, with fakes behind the service: reserving
 * (POST /api/v1/reservations), the own reservations (GET), cancelling (DELETE /{id}) and confirming
 * a pickup (POST /{id}/collect). Only a COLLECTOR may use them (§5.5, decision 3.6).
 */
class ReservationRoutesTest {

    /**
     * A clock that stands still during the test but starts at the real "now",
     * because the JWT verifier checks token expiry against the real time (#62).
     */
    private val testClock = TestClock.fixedNow()
    private val now = testClock.now()

    /** A pickup window that opened an hour ago and closes in five hours. */
    private val openWindow = PickupWindow(now - 1.hours, now + 5.hours)

    /** Shared with the app, so a test can check which status changes the service made. */
    private val statusUpdater = FakeProductStatusUpdater()

    /**
     * The real reservation routes and service, with fakes behind the service and the test clock.
     *
     * @param products what the product reader knows
     * @param reservations the reservations that are stored from the start
     * @param price the current price of every product; null means "expired"
     */
    private fun Application.testApp(
        products: List<ProductView>,
        reservations: List<Reservation> = emptyList(),
        price: Money? = Money.fromCents(299),
    ) {
        // The service opens its own transaction (GI-1, decision 1.4), so even with fakes it needs a database.
        createEmptyTestDatabase()
        configureSerialization()
        configureStatusPages()
        configureSecurity(JwtConfig(TestJwtSettings.default, testClock))
        dependencies {
            provide<ProductReader> { FakeProductReader(products) }
            provide<ProductStatusUpdater> { statusUpdater }
            provide<PriceProvider> { FakePriceProvider(price) }
            provide<ReservationRepository> { FakeReservationRepository(reservations) }
            provide<Clock> { testClock }
            provide(::ReservationService)
        }
        configureReservationRouting()
    }

    /** Starts the test app with these products, no reservations and the default price. */
    private fun ApplicationTestBuilder.setUp(vararg products: ProductView) = application { testApp(products.toList()) }

    /** A valid token for user 7 with this role. */
    private fun tokenFor(role: Role) = TestTokens().createTestToken(userId = 7L, role = role, clock = testClock)

    /** POST /api/v1/reservations for this product, with the token if one is given. */
    private suspend fun ApplicationTestBuilder.reserve(productId: Long, token: String? = null): HttpResponse =
        client.post("/api/v1/reservations") {
            token?.let { bearerAuth(it) }
            contentType(ContentType.Application.Json)
            setBody("""{"productId": $productId}""")
        }

    /** GET /api/v1/reservations, with the token if one is given. */
    private suspend fun ApplicationTestBuilder.ownReservations(token: String? = null): HttpResponse =
        client.get("/api/v1/reservations") { token?.let { bearerAuth(it) } }

    /** DELETE /api/v1/reservations/{id}; the id is text, so a test can also send one that is not a number. */
    private suspend fun ApplicationTestBuilder.cancel(reservationId: String, token: String? = null): HttpResponse =
        client.delete("/api/v1/reservations/$reservationId") { token?.let { bearerAuth(it) } }

    /** POST /api/v1/reservations/{id}/collect, with the token if one is given. */
    private suspend fun ApplicationTestBuilder.collect(reservationId: String, token: String? = null): HttpResponse =
        client.post("/api/v1/reservations/$reservationId/collect") { token?.let { bearerAuth(it) } }

    /** Reservation 1 on product 1. tokenFor() belongs to collector 7, so collectorId 7 means "mine". */
    private fun reservation(collectorId: Long = 7, status: ReservationStatus = ReservationStatus.ACTIVE) = Reservation(
        1,
        productId = 1,
        collectorId = collectorId,
        reservedPrice = Money.fromCents(299),
        reservedAt = now - 1.hours,
        expiresAt = now + 5.hours,
        status = status,
    )

    /** Product 1, RESERVED, with an open pickup window: the product of [reservation]. */
    private val reservedProduct = FakeProduct(id = 1, pickupWindow = openWindow, status = ProductStatus.RESERVED)

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
            1,
            productId = 1,
            collectorId = 7,
            reservedPrice = Money.fromCents(299),
            reservedAt = now - 1.hours,
            expiresAt = now + 5.hours,
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
            1,
            productId = 1,
            collectorId = 7,
            reservedPrice = Money.fromCents(450),
            reservedAt = now - 20.hours,
            expiresAt = now + 4.hours,
        )
        application {
            testApp(
                products = listOf(FakeProduct(id = 1, pickupWindow = openWindow)),
                reservations = listOf(reservation),
            )
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

    @Test
    fun `a collector cancels an own reservation, gets 204 and the product is listed again`() = testApplication {
        // Arrange
        application { testApp(products = listOf(reservedProduct), reservations = listOf(reservation())) }
        // Act
        val response = cancel("1", token = tokenFor(Role.COLLECTOR))
        // Assert: 204, the product went back to LISTED, and the reservation shows as CANCELLED
        assertEquals(HttpStatusCode.NoContent, response.status)
        assertTrue(1L in statusUpdater.listed, "service did not put the product back to LISTED")
        val own = Json.decodeFromString<List<ReservationResponse>>(
            ownReservations(tokenFor(Role.COLLECTOR)).bodyAsText(),
        )
        assertEquals(ReservationStatus.CANCELLED, own.single().status)
    }

    @Test
    fun `cancelling the reservation of another collector gives 403 and changes nothing`() = testApplication {
        // Arrange: the reservation belongs to collector 8, the token to collector 7
        application { testApp(products = listOf(reservedProduct), reservations = listOf(reservation(collectorId = 8))) }
        // Act
        val response = cancel("1", token = tokenFor(Role.COLLECTOR))
        // Assert
        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertTrue(statusUpdater.listed.isEmpty(), "the product of someone else was put back to LISTED")
    }

    @Test
    fun `cancelling an unknown reservation gives 404`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = cancel("999", token = tokenFor(Role.COLLECTOR))
        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `cancelling with an id that is not a number gives 400`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = cancel("abc", token = tokenFor(Role.COLLECTOR))
        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `cancelling a collected reservation gives 409 and changes nothing`() = testApplication {
        // Arrange: an own reservation that was already picked up, so its product is COLLECTED too
        val collectedProduct = FakeProduct(id = 1, pickupWindow = openWindow, status = ProductStatus.COLLECTED)
        application {
            testApp(
                products = listOf(collectedProduct),
                reservations = listOf(reservation(status = ReservationStatus.COLLECTED)),
            )
        }
        // Act
        val response = cancel("1", token = tokenFor(Role.COLLECTOR))
        // Assert: the state machine refuses CANCEL for COLLECTED (§5.6, §9.5), and nothing was written
        assertEquals(HttpStatusCode.Conflict, response.status)
        assertTrue(statusUpdater.listed.isEmpty(), "a collected product was put back to LISTED")
    }

    @Test
    fun `without a token cancelling gives 401`() = testApplication {
        // Arrange
        application { testApp(products = listOf(reservedProduct), reservations = listOf(reservation())) }
        // Act
        val response = cancel("1")
        // Assert
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `a supplier cannot cancel a reservation and gets 403`() = testApplication {
        // Arrange: a valid, active reservation, so only the role can cause the 403
        application { testApp(products = listOf(reservedProduct), reservations = listOf(reservation())) }
        // Act
        val response = cancel("1", token = tokenFor(Role.SUPPLIER))
        // Assert: requireRole stops the request before the service runs
        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertTrue(statusUpdater.listed.isEmpty(), "a supplier put the product back to LISTED")
    }

    @Test
    fun `a collector confirms the pickup inside the window and gets 200 with the reservation COLLECTED (TC-11)`() =
        testApplication {
            // Arrange: openWindow runs from 1 hour ago to 5 hours from now
            application { testApp(products = listOf(reservedProduct), reservations = listOf(reservation())) }
            // Act
            val response = collect("1", token = tokenFor(Role.COLLECTOR))
            // Assert: the moment comes from the test clock, and the product was marked COLLECTED too
            assertEquals(HttpStatusCode.OK, response.status)
            val body = Json.decodeFromString<ReservationResponse>(response.bodyAsText())
            assertEquals(ReservationStatus.COLLECTED, body.status)
            assertEquals(now.toString(), body.collectedAt)
            assertEquals(listOf(1L), statusUpdater.collected)
        }

    @Test
    fun `the confirmed pickup is stored, so the own reservations show it as COLLECTED`() = testApplication {
        // Arrange
        application { testApp(products = listOf(reservedProduct), reservations = listOf(reservation())) }
        // Act
        collect("1", token = tokenFor(Role.COLLECTOR))
        // Assert: read back through GET, not from the collect answer
        val own = Json.decodeFromString<List<ReservationResponse>>(
            ownReservations(tokenFor(Role.COLLECTOR)).bodyAsText(),
        )
        assertEquals(ReservationStatus.COLLECTED, own.single().status)
        assertEquals(now.toString(), own.single().collectedAt)
    }

    @Test
    fun `confirming before the pickup window starts gives 422 and changes nothing`() = testApplication {
        // Arrange: the window opens in 1 hour
        val notYetOpen =
            FakeProduct(
                id = 1,
                pickupWindow = PickupWindow(now + 1.hours, now + 5.hours),
                status = ProductStatus.RESERVED,
            )
        application { testApp(products = listOf(notYetOpen), reservations = listOf(reservation())) }
        // Act
        val response = collect("1", token = tokenFor(Role.COLLECTOR))
        // Assert
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertTrue(statusUpdater.collected.isEmpty(), "the product was marked COLLECTED before its window")
    }

    @Test
    fun `confirming after the pickup window ended gives 422 and changes nothing`() = testApplication {
        // Arrange: the window closed 1 hour ago, but the maintenance has not lapsed the reservation yet
        val closed =
            FakeProduct(
                id = 1,
                pickupWindow = PickupWindow(now - 5.hours, now - 1.hours),
                status = ProductStatus.RESERVED,
            )
        application { testApp(products = listOf(closed), reservations = listOf(reservation())) }
        // Act
        val response = collect("1", token = tokenFor(Role.COLLECTOR))
        // Assert
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertTrue(statusUpdater.collected.isEmpty(), "the product was marked COLLECTED after its window")
    }

    @Test
    fun `confirming exactly when the window ends is still allowed`() = testApplication {
        // Arrange: edge case, until counts as inside, like reserve() (§5.5)
        val endsNow =
            FakeProduct(id = 1, pickupWindow = PickupWindow(now - 5.hours, now), status = ProductStatus.RESERVED)
        application { testApp(products = listOf(endsNow), reservations = listOf(reservation())) }
        // Act
        val response = collect("1", token = tokenFor(Role.COLLECTOR))
        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `confirming a collected, cancelled or lapsed reservation gives 409 and changes nothing`() {
        // Each finished status with the product status it leaves behind (§9.5)
        val finished = mapOf(
            ReservationStatus.COLLECTED to ProductStatus.COLLECTED,
            ReservationStatus.CANCELLED to ProductStatus.LISTED,
            ReservationStatus.LAPSED to ProductStatus.LISTED,
        )
        for ((reservationStatus, productStatus) in finished) {
            testApplication {
                // Arrange: inside the window, so only the status can cause the refusal
                val product = FakeProduct(id = 1, pickupWindow = openWindow, status = productStatus)
                application {
                    testApp(products = listOf(product), reservations = listOf(reservation(status = reservationStatus)))
                }
                // Act
                val response = collect("1", token = tokenFor(Role.COLLECTOR))
                // Assert
                assertEquals(HttpStatusCode.Conflict, response.status, "for $reservationStatus")
                assertTrue(statusUpdater.collected.isEmpty(), "a $reservationStatus reservation was collected")
            }
        }
    }

    @Test
    fun `confirming the reservation of another collector gives 403 and changes nothing`() = testApplication {
        // Arrange: the reservation belongs to collector 8, the token to collector 7
        application { testApp(products = listOf(reservedProduct), reservations = listOf(reservation(collectorId = 8))) }
        // Act
        val response = collect("1", token = tokenFor(Role.COLLECTOR))
        // Assert
        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertTrue(statusUpdater.collected.isEmpty(), "the product of someone else was marked COLLECTED")
    }

    @Test
    fun `a supplier cannot confirm a pickup and gets 403`() = testApplication {
        // Arrange: a valid, active reservation inside the window, so only the role can cause the 403
        application { testApp(products = listOf(reservedProduct), reservations = listOf(reservation())) }
        // Act
        val response = collect("1", token = tokenFor(Role.SUPPLIER))
        // Assert: requireRole stops the request before the service runs
        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertTrue(statusUpdater.collected.isEmpty(), "a supplier marked the product COLLECTED")
    }

    @Test
    fun `without a token confirming gives 401`() = testApplication {
        // Arrange
        application { testApp(products = listOf(reservedProduct), reservations = listOf(reservation())) }
        // Act
        val response = collect("1")
        // Assert
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
