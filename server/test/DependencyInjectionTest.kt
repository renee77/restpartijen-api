package com.restpartijen.api

import com.restpartijen.api.product.aProduct
import com.restpartijen.api.product.client.OpenFoodFactsClient
import com.restpartijen.api.product.dto.ProductResponse
import com.restpartijen.api.product.fakeProductRepository
import com.restpartijen.api.product.repository.ExposedSupplierRepository
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.shared.SupplierIdLookup
import com.restpartijen.api.testsupport.testConfig
import com.restpartijen.api.testsupport.testConfigWithout
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIOEngineConfig
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Proves the GI-2 acceptance criterion: testApplication can replace a dependency
 * with a fake without changing production code (ADR-04).
 */
class DependencyInjectionTest {

    @Test
    fun `a test can replace the product repository with a fake`() = testApplication {
        // Arrange
        val fakeProduct = aProduct(id = 42, name = "Fake product")
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

    @Test
    fun `the application provides an implementation of SupplierIdLookup`() = testApplication {
        // Arrange: GI-3 asks for the contract type, not for F1's repository (A15)
        environment { config = testConfig() }
        application { module() }
        startApplication()

        // Act
        val lookup: SupplierIdLookup = application.dependencies.resolve()

        // Assert: F1's implementation, so login gets the real supplier id
        assertIs<ExposedSupplierRepository>(lookup)
    }

    @Test
    fun `the application does not start without OFF_CONTACT_EMAIL`() = testApplication {
        // Arrange: the test config without the contact address, as when the variable is missing from .env
        environment { config = testConfigWithout("openFoodFacts.contactEmail") }
        application { module() }

        // Act
        val error = assertFailsWith<IllegalStateException> { startApplication() }

        // Assert: it stops at startup, not at the first lookup, and the message names the variable (B-52)
        assertTrue(error.message.orEmpty().contains("OFF_CONTACT_EMAIL"), error.message)
    }

    @Test
    fun `the application provides the Open Food Facts client with the CIO engine`() = testApplication {
        // Arrange: the engine is registered on its own, so the DI closes it when the application stops (US-02)
        environment { config = testConfig() }
        application { module() }
        startApplication()

        // Act
        val engine: HttpClientEngine = application.dependencies.resolve()
        val client: OpenFoodFactsClient = application.dependencies.resolve()

        // Assert
        assertIs<CIOEngineConfig>(engine.config)
        assertIs<OpenFoodFactsClient>(client)
    }
}