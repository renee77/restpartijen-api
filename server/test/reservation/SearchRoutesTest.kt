package com.restpartijen.api.reservation

import com.restpartijen.api.plugins.ErrorCode
import com.restpartijen.api.plugins.ErrorResponse
import com.restpartijen.api.plugins.configureSerialization
import com.restpartijen.api.plugins.configureStatusPages
import com.restpartijen.api.reservation.dto.SearchResultResponse
import com.restpartijen.api.reservation.routes.configureSearchRouting
import com.restpartijen.api.reservation.service.ProductSearchService
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.PriceProvider
import com.restpartijen.api.shared.ProductReader
import com.restpartijen.api.shared.ProductView
import com.restpartijen.api.testsupport.FixedClock
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.provide
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * GET /api/v1/products through the route, with fakes behind the service (US-04, §5.4). No database: search opens
 * no transaction.
 */
class SearchRoutesTest {

    private val now = Instant.parse("2026-10-20T12:00:00Z")

    /** The real search route and service over these products, with every price at 1.99 and a fixed clock. */
    private fun ApplicationTestBuilder.setUp(vararg products: ProductView) = application {
        configureSerialization()
        configureStatusPages()
        dependencies {
            provide<ProductReader> { FakeProductReader(products.toList()) }
            provide<PriceProvider> { FakePriceProvider(Money.fromCents(199)) }
            provide<Clock> { FixedClock(now) }
            provide(::ProductSearchService)
        }
        configureSearchRouting()
    }

    @Test
    fun `without a token the search gives 200 with the supplier and the price in euros`() = testApplication {
        // Arrange
        setUp(FakeProduct(id = 1, pickupWindow = PickupWindow(now - 1.hours, now + 5.hours)))
        // Act
        val response = client.get("/api/v1/products")
        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val result = Json.decodeFromString<List<SearchResultResponse>>(response.bodyAsText()).single()
        assertEquals("Bakkerij De Korenaar", result.supplier.name)
        assertEquals(51.69, result.supplier.latitude)
        assertEquals(1.99, result.currentPrice)
    }

    @Test
    fun `the pickup window is a nested object, the same shape as in a product`() = testApplication {
        // Arrange
        setUp(FakeProduct(id = 1, pickupWindow = PickupWindow(now - 1.hours, now + 5.hours)))
        // Act
        val response = client.get("/api/v1/products")
        // Assert: read as raw JSON, so going back to flat fields fails here
        val product = Json.parseToJsonElement(response.bodyAsText()).jsonArray.single().jsonObject
        val window = product.getValue("pickupWindow").jsonObject
        assertEquals("2026-10-20T11:00:00Z", window.getValue("from").jsonPrimitive.content)
        assertEquals("2026-10-20T17:00:00Z", window.getValue("until").jsonPrimitive.content)
    }

    @Test
    fun `an unknown category gives 400 with the allowed values`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = client.get("/api/v1/products?category=fresh")
        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(
            ErrorResponse(
                ErrorCode.VALIDATION_ERROR,
                "Unknown category 'fresh'. Allowed values: FRESH, FROZEN, AMBIENT",
                "category",
            ),
            Json.decodeFromString<ErrorResponse>(response.bodyAsText()),
        )
    }

    @Test
    fun `a maxPrice that is not an amount gives 400 that names the field`() = testApplication {
        // Arrange
        setUp()
        // Act
        val response = client.get("/api/v1/products?maxPrice=cheap")
        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("maxPrice", Json.decodeFromString<ErrorResponse>(response.bodyAsText()).field)
    }
}
