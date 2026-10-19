package com.restpartijen.api.product

import com.restpartijen.api.plugins.configureSerialization
import com.restpartijen.api.plugins.configureStatusPages
import com.restpartijen.api.product.dto.ProductResponse
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.product.repository.SupplierRepository
import com.restpartijen.api.product.routes.configureProductRouting
import com.restpartijen.api.product.service.ProductService
import com.restpartijen.api.security.TestJwtSettings
import com.restpartijen.api.security.TestTokens
import com.restpartijen.api.security.service.JwtConfig
import com.restpartijen.api.security.service.configureSecurity
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.FixedClock
import io.ktor.client.request.bearerAuth
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
import kotlin.time.Clock

/**
 * POST /api/v1/products through all layers, with fakes behind the service (US-01, §5.1).
 * Only a SUPPLIER may place a product (decision 3.6).
 */
class PlaceProductRouteTest {

    // The user id in every token; the fake supplier repository knows a supplier for this user.
    private val supplierUserId = 7L

    private fun Application.testApp() {
        configureSerialization()
        configureStatusPages()
        configureSecurity(JwtConfig(TestJwtSettings.default, Clock.System))
        dependencies {
            provide<ProductRepository> { fakeProductRepository() }
            provide<SupplierRepository> { fakeSupplierRepository(aSupplier, ownerUserId = supplierUserId) }
            // The listing rules use a fixed "now"; the token below uses the real clock, because the JWT verifier does.
            provide<Clock> { FixedClock(testNow) }
            provide(::ProductService)
        }
        configureProductRouting()
    }

    private fun tokenFor(role: Role) =
        TestTokens().createTestToken(userId = supplierUserId, role = role, clock = Clock.System)

    // A valid body: best-before three days after testNow, pickup window inside it.
    private val validBody = """
        {"category": "FRESH", "name": "Volkoren brood", "originalPrice": 3.49,
         "bestBeforeAt": "2026-10-04T00:00:00Z", "quantity": 2,
         "pickupWindow": {"from": "2026-10-01T09:00:00Z", "until": "2026-10-01T17:00:00Z"},
         "allergens": ["CEREALS_CONTAINING_GLUTEN"]}
    """

    private suspend fun ApplicationTestBuilder.place(body: String = validBody, token: String? = null): HttpResponse =
        client.post("/api/v1/products") {
            token?.let { bearerAuth(it) }
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    @Test
    fun `a supplier places a product and gets 201 with the product, status LISTED`() = testApplication {
        // Arrange
        application { testApp() }

        // Act
        val response = place(token = tokenFor(Role.SUPPLIER))

        // Assert: TC-01
        assertEquals(HttpStatusCode.Created, response.status)
        val product = Json.decodeFromString<ProductResponse>(response.bodyAsText())
        assertEquals("LISTED", product.status)
        assertEquals(aSupplier.id, product.supplierId)
        assertEquals(3.49, product.originalPrice)
    }

    @Test
    fun `a collector gets 403`() = testApplication {
        // Arrange
        application { testApp() }

        // Act
        val response = place(token = tokenFor(Role.COLLECTOR))

        // Assert
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `without a token the answer is 401`() = testApplication {
        // Arrange
        application { testApp() }

        // Act
        val response = place(token = null)

        // Assert
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
