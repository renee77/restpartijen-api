package com.restpartijen.api

import com.restpartijen.api.product.fakeProductRepository
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

/**
 * Proves the GI-2 acceptance criterion: testApplication can replace a dependency
 * with a fake without changing production code (ADR-04).
 */
class DependencyInjectionTest {

    @Test
    fun `a test can replace the product repository with a fake`() = testApplication {
        // Arrange
        val fakeProduct = Product(id = 42, name = "Fake product", category = "FRESH")
        environment { config = testConfig() }
        application {
            // Registered before module(): in tests, Ktor ignores the conflicting
            // registration in configureDependencies(), so the fake wins.
            dependencies.provide<ProductRepository> { fakeProductRepository(fakeProduct) }
            module()
        }

        // Act
        val response = client.get("/api/v1/products/42")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.decodeFromString<ProductResponse>(response.bodyAsText())
        assertEquals("Fake product", body.name)
    }
}