package com.restpartijen.api.product

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.plugins.ErrorResponse
import com.restpartijen.api.product.dto.ProductResponse
import com.restpartijen.api.product.repository.ExposedProductRepository
import com.restpartijen.api.product.repository.ExposedSupplierRepository
import com.restpartijen.api.product.repository.ProductAllergensTable
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.FixedClock
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import com.restpartijen.api.testsupport.insertProduct
import com.restpartijen.api.testsupport.insertSupplier
import com.restpartijen.api.testsupport.insertUser
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * PUT /api/v1/products/{id} through all layers (US-03, A13, TC-03).
 * Order of the checks: role (401/403), body shape (400), product exists (404), own product (403),
 * LISTED (409), listing rules (422); then 200 with the updated product.
 */
class UpdateProductRouteTest {

    // Fixed during the test (GI-6, decision 6.5), but at the real "now" (#62); see productRoutesTestApp.
    private val tokenClock = FixedClock(Clock.System.now())

    private fun tokenFor(role: Role, userId: Long = supplierUserId) = productToken(role, tokenClock, userId)

    // A valid body: every field of POST except category, best-before three days after testNow.
    private val validBody = """
        {"name": "Wit brood", "originalPrice": 2.49,
         "bestBeforeAt": "2026-10-04", "quantity": 5,
         "pickupWindow": {"from": "2026-10-01T09:00:00Z", "until": "2026-10-01T17:00:00Z"},
         "allergens": ["MILK"]}
    """

    private suspend fun ApplicationTestBuilder.update(
        id: String,
        body: String = validBody,
        token: String? = null,
    ): HttpResponse =
        client.put("/api/v1/products/$id") {
            token?.let { bearerAuth(it) }
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    @Test
    fun `a supplier updates an own product and gets 200 with the updated product`() = testApplication {
        // Arrange: TC-03
        application {
            productRoutesTestApp(
                tokenClock, fakeProductRepository(aProduct(id = 1, category = ProductCategory.FROZEN)),
            )
        }

        // Act
        val response = update("1", token = tokenFor(Role.SUPPLIER))

        // Assert: the new values, while id, category and status stay
        assertEquals(HttpStatusCode.OK, response.status)
        val product = Json.decodeFromString<ProductResponse>(response.bodyAsText())
        assertEquals(1L, product.id)
        assertEquals("Wit brood", product.name)
        assertEquals(2.49, product.originalPrice)
        assertEquals("FROZEN", product.category)
        assertEquals("LISTED", product.status)
    }

    @Test
    fun `the update reaches the database, with the price in cents`() = testApplication {
        // Arrange: a real database and the Exposed repositories behind the service
        createEmptyTestDatabase(UsersTable, SuppliersTable, ProductsTable, ProductAllergensTable)
        val userId = insertUser(Role.SUPPLIER)
        val productId = insertProduct(supplierId = insertSupplier(userId))
        application {
            productRoutesTestApp(tokenClock, ExposedProductRepository(FixedClock(testNow)), ExposedSupplierRepository())
        }

        // Act
        val response = update("$productId", token = tokenFor(Role.SUPPLIER, userId))

        // Assert: read the row itself, so the test sees what the database holds
        assertEquals(HttpStatusCode.OK, response.status)
        val row = transaction { ProductsTable.selectAll().where { ProductsTable.id eq productId }.single() }
        assertEquals("Wit brood", row[ProductsTable.name])
        assertEquals(249L, row[ProductsTable.originalPriceCents])
    }

    @Test
    fun `a body with a category gives 400, because the category cannot change`() = testApplication {
        // Arrange: A13; the JSON reader refuses a field the request does not have
        application { productRoutesTestApp(tokenClock, fakeProductRepository(aProduct(id = 1))) }
        val bodyWithCategory = validBody.replace("{\"name\"", "{\"category\": \"FROZEN\", \"name\"")

        // Act
        val response = update("1", body = bodyWithCategory, token = tokenFor(Role.SUPPLIER))

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `an empty name gives 400 with the field`() = testApplication {
        // Arrange: the shape rules of POST also hold for PUT (RequestValidation)
        application { productRoutesTestApp(tokenClock, fakeProductRepository(aProduct(id = 1))) }

        // Act
        val response = update(
            "1", body = validBody.replace("\"Wit brood\"", "\"\""), token = tokenFor(Role.SUPPLIER),
        )

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("name", Json.decodeFromString<ErrorResponse>(response.bodyAsText()).field)
    }

    @Test
    fun `without a token the answer is 401`() = testApplication {
        application { productRoutesTestApp(tokenClock, fakeProductRepository(aProduct(id = 1))) }

        assertEquals(HttpStatusCode.Unauthorized, update("1").status)
    }

    @Test
    fun `a collector gets 403`() = testApplication {
        application { productRoutesTestApp(tokenClock, fakeProductRepository(aProduct(id = 1))) }

        assertEquals(HttpStatusCode.Forbidden, update("1", token = tokenFor(Role.COLLECTOR)).status)
    }

    @Test
    fun `a supplier gets 403 for a product of another supplier`() = testApplication {
        // Arrange: the token belongs to aSupplier (user 7); the product to supplier 2
        application {
            productRoutesTestApp(tokenClock, fakeProductRepository(aProduct(id = 1, supplier = aSupplier.copy(id = 2))))
        }

        assertEquals(HttpStatusCode.Forbidden, update("1", token = tokenFor(Role.SUPPLIER)).status)
    }

    @Test
    fun `an unknown product gives 404`() = testApplication {
        application { productRoutesTestApp(tokenClock) }

        assertEquals(HttpStatusCode.NotFound, update("999", token = tokenFor(Role.SUPPLIER)).status)
    }

    @Test
    fun `a RESERVED product gives 409`() = testApplication {
        // Arrange: A13, only a LISTED product can change
        application {
            productRoutesTestApp(tokenClock, fakeProductRepository(aProduct(id = 1, status = ProductStatus.RESERVED)))
        }

        assertEquals(HttpStatusCode.Conflict, update("1", token = tokenFor(Role.SUPPLIER)).status)
    }

    @Test
    fun `a best-before moment in the past gives 422`() = testApplication {
        // Arrange: a domain rule (validateForListing), not a shape error; the window ends before it
        application { productRoutesTestApp(tokenClock, fakeProductRepository(aProduct(id = 1))) }
        val body = validBody
            .replace("2026-10-04", "2026-09-30")
            .replace("2026-10-01T09:00:00Z", "2026-09-29T09:00:00Z")
            .replace("2026-10-01T17:00:00Z", "2026-09-29T17:00:00Z")

        // Act
        val response = update("1", body = body, token = tokenFor(Role.SUPPLIER))

        // Assert
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals("bestBeforeAt", Json.decodeFromString<ErrorResponse>(response.bodyAsText()).field)
    }

    @Test
    fun `a product id that is not a number gives 400`() = testApplication {
        application { productRoutesTestApp(tokenClock) }

        val response = update("abc", token = tokenFor(Role.SUPPLIER))

        // With the field, like the other routes (E-05)
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("id", Json.decodeFromString<ErrorResponse>(response.bodyAsText()).field)
    }
}
