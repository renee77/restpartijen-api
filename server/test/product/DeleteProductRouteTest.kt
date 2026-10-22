package com.restpartijen.api.product

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.product.repository.ExposedProductRepository
import com.restpartijen.api.product.repository.ExposedSupplierRepository
import com.restpartijen.api.product.repository.ProductAllergensTable
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.FixedClock
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import com.restpartijen.api.testsupport.insertProduct
import com.restpartijen.api.testsupport.insertSupplier
import com.restpartijen.api.testsupport.insertUser
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * DELETE /api/v1/products/{id} through all layers (US-03, §5.3).
 * Order of the checks: role (401/403), product exists (404), own product (403), LISTED (409); then 204.
 */
class DeleteProductRouteTest {

    // Fixed during the test (GI-6, decision 6.5), but at the real "now" (#62); see productRoutesTestApp.
    private val tokenClock = FixedClock(Clock.System.now())

    private fun tokenFor(role: Role, userId: Long = supplierUserId) = productToken(role, tokenClock, userId)

    private suspend fun ApplicationTestBuilder.deleteProduct(id: String, token: String? = null): HttpResponse =
        client.delete("/api/v1/products/$id") { token?.let { bearerAuth(it) } }

    // A real database with one supplier and the Exposed repositories, for the tests that check the stored status.
    // Returns the user id of the supplier and the id of its LISTED product.
    private fun databaseWithProduct(): Pair<Long, Long> {
        createEmptyTestDatabase(UsersTable, SuppliersTable, ProductsTable, ProductAllergensTable)
        val userId = insertUser(Role.SUPPLIER)
        val productId = insertProduct(supplierId = insertSupplier(userId))
        return userId to productId
    }

    @Test
    fun `a supplier deletes an own LISTED product and gets 204, the row stays with status REMOVED`() = testApplication {
        // Arrange
        val (userId, productId) = databaseWithProduct()
        application {
            productRoutesTestApp(
                tokenClock, ExposedProductRepository(FixedClock(testNow)), ExposedSupplierRepository(),
            )
        }

        // Act
        val response = deleteProduct("$productId", tokenFor(Role.SUPPLIER, userId))

        // Assert: soft delete (§9.5, B-19); statusOf reads the table itself, past the repository
        assertEquals(HttpStatusCode.NoContent, response.status)
        assertEquals(ProductStatus.REMOVED, statusOf(productId))
    }

    @Test
    fun `deleting an already REMOVED product gives 404`() = testApplication {
        // Arrange: a REMOVED product counts as not existing (§5.3, issue #45)
        val (userId, productId) = databaseWithProduct()
        setStatus(productId, ProductStatus.REMOVED)
        application {
            productRoutesTestApp(
                tokenClock, ExposedProductRepository(FixedClock(testNow)), ExposedSupplierRepository(),
            )
        }

        // Act
        val response = deleteProduct("$productId", tokenFor(Role.SUPPLIER, userId))

        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `without a token the answer is 401`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock, fakeProductRepository(aProduct(id = 1))) }

        // Act
        val response = deleteProduct("1")

        // Assert
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `a collector gets 403`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock, fakeProductRepository(aProduct(id = 1))) }

        // Act
        val response = deleteProduct("1", tokenFor(Role.COLLECTOR))

        // Assert
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `a supplier gets 403 for a product of another supplier`() = testApplication {
        // Arrange: the token belongs to aSupplier (user 7); the product to supplier 2
        application {
            productRoutesTestApp(tokenClock, fakeProductRepository(aProduct(id = 1, supplier = aSupplier.copy(id = 2))))
        }

        // Act
        val response = deleteProduct("1", tokenFor(Role.SUPPLIER))

        // Assert
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `an unknown product gives 404`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = deleteProduct("999", tokenFor(Role.SUPPLIER))

        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `a RESERVED product gives 409`() = testApplication {
        // Arrange: only LISTED can be deleted (§5.3)
        application {
            productRoutesTestApp(tokenClock, fakeProductRepository(aProduct(id = 1, status = ProductStatus.RESERVED)))
        }

        // Act
        val response = deleteProduct("1", tokenFor(Role.SUPPLIER))

        // Assert
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `a product id that is not a number gives 400`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = deleteProduct("abc", tokenFor(Role.SUPPLIER))

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
