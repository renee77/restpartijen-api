package com.restpartijen.api.product

import com.restpartijen.api.plugins.ErrorResponse
import com.restpartijen.api.product.dto.ProductResponse
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.testsupport.FixedClock
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock

/**
 * The public GET routes of F1 for one product and for the offer of a supplier, with fakes behind the service
 * (§5.7, A4). The public GET of the opening hours is in OpeningHoursRoutesTest.
 * Only the F1 routes start, without the seed data of module(), so each test sees only its own products (E-09).
 */
class ProductRoutesTest {

    /** The GET routes need no token, but productRoutesTestApp also starts the security plugin. */
    private val tokenClock = FixedClock(Clock.System.now())

    @Test
    fun `GET existing product returns 200 with the product as JSON`() = testApplication {
        // Arrange
        application {
            productRoutesTestApp(tokenClock, fakeProductRepository(aProduct(id = 1, name = "Volkoren brood")))
        }

        // Act
        val response = client.get("/api/v1/products/1")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.decodeFromString<ProductResponse>(response.bodyAsText())
        assertEquals("Volkoren brood", body.name)
    }

    @Test
    fun `GET product shows the price breakdown`() = testApplication {
        // Arrange: §5.7 and §10.1, original price, discount and current price in the detail
        application { productRoutesTestApp(tokenClock, fakeProductRepository(aProduct(id = 1))) }

        // Act
        val response = client.get("/api/v1/products/1")

        // Assert: the values of aBreakdown, in euros (B-25)
        val body = Json.decodeFromString<ProductResponse>(response.bodyAsText())
        assertEquals(3.50, body.originalPrice)
        assertEquals(40, body.discountPercentage)
        assertEquals(2.10, body.currentPrice)
    }

    @Test
    fun `GET expired product returns 200 with status EXPIRED and no price`() = testApplication {
        // Arrange: SD-3, no price means expired; the product is still LISTED in storage
        application {
            productRoutesTestApp(
                tokenClock,
                fakeProductRepository(aProduct(id = 1)),
                priceProvider = FixedPriceProvider(breakdown = null),
            )
        }

        // Act
        val response = client.get("/api/v1/products/1")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.decodeFromString<ProductResponse>(response.bodyAsText())
        assertEquals("EXPIRED", body.status)
        assertNull(body.discountPercentage)
        assertNull(body.currentPrice)
    }

    @Test
    fun `GET unknown product returns 404`() = testApplication {
        // Arrange: a repository without products
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = client.get("/api/v1/products/999")

        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET product with a non-numeric id returns 400 and names the field`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = client.get("/api/v1/products/abc")

        // Assert: the route throws ValidationException with the field, like PricingRoutes does (E-05)
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("id", Json.decodeFromString<ErrorResponse>(response.bodyAsText()).field)
    }

    @Test
    fun `GET supplier products returns 200 with the current offer as JSON, without a token`() = testApplication {
        // Arrange: the route is public (A4), so the request below has no token.
        // The fake repository applies the same filter as the real query: a COLLECTED product stays out.
        val products = fakeProductRepository(
            aProduct(id = 1, supplier = aSupplier, name = "Volkoren brood"),
            aProduct(id = 2, supplier = aSupplier, name = "Croissant", status = ProductStatus.RESERVED),
            aProduct(id = 3, supplier = aSupplier, status = ProductStatus.COLLECTED),
        )
        application { productRoutesTestApp(tokenClock, products) }

        // Act
        val response = client.get("/api/v1/suppliers/${aSupplier.id}/products")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.decodeFromString<List<ProductResponse>>(response.bodyAsText())
        assertEquals(listOf("Volkoren brood", "Croissant"), body.map { it.name })
        assertEquals(listOf("LISTED", "RESERVED"), body.map { it.status })
        assertEquals(listOf(2.10, 2.10), body.map { it.currentPrice })
    }

    @Test
    fun `GET supplier products of an unknown supplier returns 404`() = testApplication {
        // Arrange: the fake supplier repository knows only aSupplier
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = client.get("/api/v1/suppliers/999/products")

        // Assert: the service throws NotFoundException; StatusPages turns it into a 404
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET supplier products with a non-numeric id returns 400 and names the field`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = client.get("/api/v1/suppliers/abc/products")

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("id", Json.decodeFromString<ErrorResponse>(response.bodyAsText()).field)
    }
}
