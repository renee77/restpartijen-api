package com.restpartijen.api.product

import com.restpartijen.api.module
import com.restpartijen.api.product.dto.ProductResponse
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.shared.PriceProvider
import com.restpartijen.api.testsupport.testConfig
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductRoutesTest {
    @Test
    fun `GET existing product returns 200 with the product as JSON`() = testApplication {
        // Arrange: testApplication does not load application.yaml, so the config comes from testConfig().
        // There is no seed data yet, so the product comes from a fake repository.
        environment { config = testConfig() }
        application {
            dependencies.provide<ProductRepository> {
                fakeProductRepository(aProduct(id = 1, name = "Volkoren brood"))
            }
            module()
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
        environment { config = testConfig() }
        application {
            dependencies.provide<ProductRepository> { fakeProductRepository(aProduct(id = 1)) }
            dependencies.provide<PriceProvider> { FixedPriceProvider(aBreakdown) }
            module()
        }

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
        environment { config = testConfig() }
        application {
            dependencies.provide<ProductRepository> { fakeProductRepository(aProduct(id = 1)) }
            dependencies.provide<PriceProvider> { FixedPriceProvider(breakdown = null) }
            module()
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
        // Arrange: the real repository on an empty in-memory database
        environment { config = testConfig() }
        application { module() }
        // Act
        val response = client.get("/api/v1/products/999")
        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET product with a non-numeric id returns 400`() = testApplication {
        // Arrange
        environment { config = testConfig() }
        application { module() }
        // Act
        val response = client.get("/api/v1/products/abc")
        // Assert: the route throws ValidationException; StatusPages turns it into a 400
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
