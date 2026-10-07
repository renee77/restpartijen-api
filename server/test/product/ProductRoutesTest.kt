package com.restpartijen.api.product

import com.restpartijen.api.module
import com.restpartijen.api.product.dto.ProductResponse
import com.restpartijen.api.product.model.Product
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.testsupport.testConfig
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductRoutesTest {
    @Test
    fun `GET existing product returns 200 with the product as JSON`() = testApplication {
        // Arrange: testApplication does not load application.yaml, so the config comes from testConfig().
        // There is no seed data yet, so the product comes from a fake repository.
        environment { config = testConfig() }
        application {
            dependencies.provide<ProductRepository> {
                fakeProductRepository(Product(id = 1, name = "Volkoren brood", category = "FRESH"))
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
    fun `GET unknown product returns 404`() = testApplication {
        // Arrange: the real repository on an empty in-memory database
        environment { config = testConfig() }
        application { module() }
        // Act
        val response = client.get("/api/v1/products/999")
        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
