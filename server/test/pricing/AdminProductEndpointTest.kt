package com.restpartijen.api.pricing

import com.restpartijen.api.plugins.ErrorCode
import com.restpartijen.api.plugins.ErrorResponse
import com.restpartijen.api.pricing.dto.AdminProductResponse
import com.restpartijen.api.security.TestClock
import com.restpartijen.api.security.TestTokens
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdminProductEndpointTest {
    val token = TestTokens().createTestTokenWithRoleName(userId = 1L, roleName = "ADMIN")

    private fun tokenFor(role: String) = TestTokens().createTestTokenWithRoleName(userId = 1L, roleName = role)

    @Test
    fun `No token gives a 401`() = testApplication {
        // Arrange: test app with the real pricing round and fakes behind the service.
        application { setUpPricingTestApp(productReader = FakeProductReader(emptyList())) }

        // Act: call the endpoint without a token
        val response = client.get("/api/v1/admin/products")

        // Assert: the response status should be 401 Unauthorized, indicating that the token's role is not valid
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("Authentication required", error.message)
    }

    @Test
    fun `collector token returns 403`() = testApplication {
        // Arrange
        application {
            setUpPricingTestApp(productReader = FakeProductReader(emptyList()))
        }

        val collectorToken = TestTokens().createTestTokenWithRoleName(userId = 1L, roleName = "COLLECTOR")

        // Act
        val response = client.get("/api/v1/admin/products") {
            bearerAuth(collectorToken)
        }

        // Assert. Verify that the decoded token contains the expected userId and role.
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText()).message
        assertEquals("Insufficient role", error)
    }

    @Test
    fun `admin token returns 200`() = testApplication {
        application {
            setUpPricingTestApp()
        }

        val response = client.get("/api/v1/admin/products") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `status REMOVED shows only the removed products`() = testApplication {
        // Arrange: two products, one of them removed
        val reader = FakeProductReader(
            products = listOf(
                FakeProductView(1, ProductStatus.LISTED),
                FakeProductView(2, ProductStatus.REMOVED),
            ),
        )
        application { setUpPricingTestApp(productReader = reader) }

        // Act
        val response = client.get("/api/v1/admin/products?status=REMOVED") {
            bearerAuth(token)
        }

        // Assert: 200, only the removed product, and the service asked for exactly REMOVED
        assertEquals(HttpStatusCode.OK, response.status)
        val products = Json.decodeFromString<List<AdminProductResponse>>(response.bodyAsText())
        assertEquals(listOf(2L), products.map { it.id })
        assertEquals(setOf(ProductStatus.REMOVED), reader.requestedStatuses)
    }

    @Test
    fun `status listed only shows the listed products`() = testApplication {
        // Arrange
        val reader = FakeProductReader(
            products = listOf(
                FakeProductView(1, ProductStatus.LISTED),
                FakeProductView(2, ProductStatus.COLLECTED),
            ),
        )
        application { setUpPricingTestApp(productReader = reader) }

        // Act
        val response = client.get("/api/v1/admin/products?status=LISTED") {
            bearerAuth(token)
        }

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val products = Json.decodeFromString<List<AdminProductResponse>>(response.bodyAsText())
        assertEquals(listOf(1L), products.map { it.id })
        assertEquals(setOf(ProductStatus.LISTED), reader.requestedStatuses)
    }

    @Test
    fun `Call without status shows all products save for the removed ones`() = testApplication {
        // Arrange
        val reader = FakeProductReader(
            products = listOf(
                FakeProductView(1, ProductStatus.LISTED),
                FakeProductView(2, ProductStatus.COLLECTED),
                FakeProductView(3, ProductStatus.REMOVED),
                FakeProductView(4, ProductStatus.RESERVED),
                FakeProductView(5, ProductStatus.EXPIRED),
            ),
        )
        application { setUpPricingTestApp(productReader = reader) }

        // Act
        val response = client.get("/api/v1/admin/products") {
            bearerAuth(token)
        }

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val products = Json.decodeFromString<List<AdminProductResponse>>(response.bodyAsText())
        assertEquals(listOf(1L, 2L, 4L, 5L), products.map { it.id })
        assertEquals(
            setOf(ProductStatus.LISTED, ProductStatus.COLLECTED, ProductStatus.EXPIRED, ProductStatus.RESERVED),
            reader.requestedStatuses,
        )
    }

    @Test
    fun `Calls with two statuses will show all the products that have that status`() = testApplication {
        // Arrange
        val reader = FakeProductReader(
            products = listOf(
                FakeProductView(1, ProductStatus.LISTED),
                FakeProductView(2, ProductStatus.COLLECTED),
                FakeProductView(3, ProductStatus.REMOVED),
                FakeProductView(4, ProductStatus.RESERVED),
                FakeProductView(5, ProductStatus.EXPIRED),
            ),
        )
        application { setUpPricingTestApp(productReader = reader) }

        // Act
        val response = client.get("/api/v1/admin/products?Status=LISTED&Status=COLLECTED") {
            bearerAuth(token)
        }

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val products = Json.decodeFromString<List<AdminProductResponse>>(response.bodyAsText())
        assertEquals(listOf(1L, 2L), products.map { it.id })
        assertEquals(setOf(ProductStatus.LISTED, ProductStatus.COLLECTED), reader.requestedStatuses)
    }

    @Test
    fun `unknown status gives 400 with the allowed values and field status`() = testApplication {
        // Arrange
        application { setUpPricingTestApp() }

        // Act
        val response = client.get("/api/v1/admin/products?status=BESTAATNIET") {
            bearerAuth(token)
        }

        // Assert: 400 in the error model, the message lists every allowed status, and field names the parameter
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals(ErrorCode.VALIDATION_ERROR, error.code)
        assertEquals("status", error.field)
        ProductStatus.entries.forEach { status ->
            assertTrue(error.message.contains(status.name), "message should mention ${status.name}")
        }
    }

    @Test
    fun `supplier token returns 403`() = testApplication {
        // Arrange
        application { setUpPricingTestApp() }

        // Act
        val response = client.get("/api/v1/admin/products") {
            bearerAuth(tokenFor("SUPPLIER"))
        }

        // Assert
        assertEquals(HttpStatusCode.Forbidden, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals(ErrorCode.FORBIDDEN, error.code)
    }

    @Test
    fun `the response contains the fields of the product`() = testApplication {
        // Arrange: one known product
        val reader = FakeProductReader(products = listOf(FakeProductView(7, ProductStatus.RESERVED)))
        application { setUpPricingTestApp(productReader = reader) }

        // Act
        val response = client.get("/api/v1/admin/products") {
            bearerAuth(token)
        }

        // Assert: every field matches what FakeProductView holds
        assertEquals(HttpStatusCode.OK, response.status)
        val product = Json.decodeFromString<List<AdminProductResponse>>(response.bodyAsText()).single()
        assertEquals(7L, product.id)
        assertEquals("Fake product", product.name)
        assertEquals(ProductStatus.RESERVED, product.status)
        assertEquals("Fake supplier", product.supplierName)
        assertEquals("2026-01-01T10:00:00Z", product.pickupWindowStart)
        assertEquals("2026-01-01T12:00:00Z", product.pickupWindowEnd)
    }

    @Test
    fun `admin deletes a listed product and gets 204`() = testApplication {
        // Arrange
        createEmptyTestDatabase()
        val updater = FakeProductStatusUpdater()
        application {
            setUpPricingTestApp(
                productReader = FakeProductReader(products = listOf(FakeProductView(1, ProductStatus.LISTED))),
                productStatusUpdater = updater,
            )
        }

        // Act
        val response = client.delete("/api/v1/admin/products/1") { bearerAuth(token) }

        // Assert
        assertEquals(HttpStatusCode.NoContent, response.status)
        assertEquals(listOf(1L), updater.removedIds)
    }

    @Test
    fun `delete without a token gives 401`() = testApplication {
        application { setUpPricingTestApp() }

        val response = client.delete("/api/v1/admin/products/1")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals(ErrorCode.UNAUTHORIZED, error.code)
    }

    @Test
    fun `delete with a collector token gives 403`() = testApplication {
        application { setUpPricingTestApp() }

        val response = client.delete("/api/v1/admin/products/1") { bearerAuth(tokenFor("COLLECTOR")) }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals(ErrorCode.FORBIDDEN, error.code)
    }

    @Test
    fun `delete with a supplier token gives 403`() = testApplication {
        application { setUpPricingTestApp() }

        val response = client.delete("/api/v1/admin/products/1") { bearerAuth(tokenFor("SUPPLIER")) }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals(ErrorCode.FORBIDDEN, error.code)
    }

    @Test
    fun `deleting an unknown product gives 404`() = testApplication {
        createEmptyTestDatabase()
        application { setUpPricingTestApp(productReader = FakeProductReader()) }

        val response = client.delete("/api/v1/admin/products/99") { bearerAuth(token) }

        assertEquals(HttpStatusCode.NotFound, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals(ErrorCode.NOT_FOUND, error.code)
    }

    @Test
    fun `deleting an already removed product gives 404`() = testApplication {
        // The reader returns null for REMOVED (B-32), so this is a 404 and not a 409
        createEmptyTestDatabase()
        application {
            setUpPricingTestApp(
                productReader = FakeProductReader(products = listOf(FakeProductView(1, ProductStatus.REMOVED))),
            )
        }

        val response = client.delete("/api/v1/admin/products/1") { bearerAuth(tokenFor("ADMIN")) }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `deleting a collected product gives 409 and removes nothing`() = testApplication {
        createEmptyTestDatabase()
        val updater = FakeProductStatusUpdater()
        application {
            setUpPricingTestApp(
                productReader = FakeProductReader(products = listOf(FakeProductView(1, ProductStatus.COLLECTED))),
                productStatusUpdater = updater,
            )
        }

        val response = client.delete("/api/v1/admin/products/1") { bearerAuth(token) }

        assertEquals(HttpStatusCode.Conflict, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals(ErrorCode.CONFLICT, error.code)
        assertTrue(updater.removedIds.isEmpty())
    }

    @Test
    fun `deleting an expired product gives 409 and removes nothing`() = testApplication {
        createEmptyTestDatabase()
        val updater = FakeProductStatusUpdater()
        application {
            setUpPricingTestApp(
                productReader = FakeProductReader(products = listOf(FakeProductView(1, ProductStatus.EXPIRED))),
                productStatusUpdater = updater,
            )
        }

        val response = client.delete("/api/v1/admin/products/1") { bearerAuth(token) }

        assertEquals(HttpStatusCode.Conflict, response.status)
        assertTrue(updater.removedIds.isEmpty())
    }

    @Test
    fun `a product id that is not a number gives 400`() = testApplication {
        application { setUpPricingTestApp() }

        val response = client.delete("/api/v1/admin/products/abc") { bearerAuth(token) }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals(ErrorCode.VALIDATION_ERROR, error.code)
        assertEquals("id", error.field)
    }

    @Test
    fun `deleting a reserved product lapses the reservation first`() = testApplication {
        // Arrange: one shared call list across both fakes shows the order
        createEmptyTestDatabase()
        val calls = mutableListOf<String>()
        application {
            setUpPricingTestApp(
                reservationMaintenance = FakeReservationMaintenance(calls = calls),
                productReader = FakeProductReader(products = listOf(FakeProductView(1, ProductStatus.RESERVED))),
                productStatusUpdater = FakeProductStatusUpdater(calls = calls),
            )
        }

        // Act
        val response = client.delete("/api/v1/admin/products/1") { bearerAuth(token) }

        // Assert
        assertEquals(HttpStatusCode.NoContent, response.status)
        assertEquals(listOf("lapseActiveFor(1)", "markRemoved(1)"), calls)
    }
}
