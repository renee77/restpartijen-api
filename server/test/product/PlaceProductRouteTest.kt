package com.restpartijen.api.product

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.plugins.ErrorResponse
import com.restpartijen.api.plugins.configureRequestValidation
import com.restpartijen.api.plugins.configureSerialization
import com.restpartijen.api.plugins.configureStatusPages
import com.restpartijen.api.product.dto.ProductResponse
import com.restpartijen.api.product.repository.ExposedProductRepository
import com.restpartijen.api.product.repository.ExposedSupplierRepository
import com.restpartijen.api.product.repository.ProductAllergensTable
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.product.repository.SupplierRepository
import com.restpartijen.api.product.routes.configureProductRouting
import com.restpartijen.api.product.service.ProductService
import com.restpartijen.api.security.TestJwtSettings
import com.restpartijen.api.security.TestTokens
import com.restpartijen.api.security.service.JwtConfig
import com.restpartijen.api.security.service.configureSecurity
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.PriceProvider
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.FixedClock
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import com.restpartijen.api.testsupport.insertSupplier
import com.restpartijen.api.testsupport.insertUser
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
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.assertTrue
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

    // Fakes by default; the database test passes the Exposed repositories instead.
    private fun Application.testApp(
        products: ProductRepository = fakeProductRepository(),
        suppliers: SupplierRepository = fakeSupplierRepository(aSupplier, ownerUserId = supplierUserId),
    ) {
        configureSerialization()
        configureStatusPages()
        configureRequestValidation()
        configureSecurity(JwtConfig(TestJwtSettings.default, Clock.System))
        dependencies {
            provide<ProductRepository> { products }
            provide<SupplierRepository> { suppliers }
            // The listing rules use a fixed "now"; the token below uses the real clock, because the JWT verifier does.
            provide<Clock> { FixedClock(testNow) }
            provide<PriceProvider> { FixedPriceProvider(aBreakdown) }
            provide(::ProductService)
        }
        configureProductRouting()
    }

    private fun tokenFor(role: Role, userId: Long = supplierUserId) =
        TestTokens().createTestToken(userId = userId, role = role, clock = Clock.System)

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
    fun `the answer to placing a product includes the price breakdown`() = testApplication {
        // Arrange: §10.1, every answer with a product has the three price fields
        application { testApp() }

        // Act
        val response = place(token = tokenFor(Role.SUPPLIER))

        // Assert: the values of aBreakdown, in euros (B-25)
        val product = Json.decodeFromString<ProductResponse>(response.bodyAsText())
        assertEquals(40, product.discountPercentage)
        assertEquals(2.10, product.currentPrice)
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

    @Test
    fun `without bestBeforeAt the answer is 400 and names the field`() = testApplication {
        // Arrange: validBody without the bestBeforeAt line
        application { testApp() }
        val body = """
      {"category": "FRESH", "name": "Volkoren brood", "originalPrice": 3.49,
       "quantity": 2,
       "pickupWindow": {"from": "2026-10-01T09:00:00Z", "until": "2026-10-01T17:00:00Z"},
       "allergens": ["CEREALS_CONTAINING_GLUTEN"]}
  """

        // Act
        val response = place(body = body, token = tokenFor(Role.SUPPLIER))

        // Assert: the status, and the field in the ErrorResponse
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("bestBeforeAt", error.field)
    }

    @Test
    fun `an unknown allergen gives 400`() = testApplication {
        // Arrange: CHOCOLATE is not one of the 14 allergens
        application { testApp() }
        val body = """
            {"category": "FRESH", "name": "Volkoren brood", "originalPrice": 3.49,
             "bestBeforeAt": "2026-10-04T00:00:00Z", "quantity": 2,
             "pickupWindow": {"from": "2026-10-01T09:00:00Z", "until": "2026-10-01T17:00:00Z"},
             "allergens": ["CHOCOLATE"]}
        """

        // Act
        val response = place(body = body, token = tokenFor(Role.SUPPLIER))

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `an empty name gives 400 and names the field`() = testApplication {
        // Arrange: validBody with an empty name (E-03: was stored as is)
        application { testApp() }
        val body = validBody.replace("\"Volkoren brood\"", "\"\"")

        // Act
        val response = place(body = body, token = tokenFor(Role.SUPPLIER))

        // Assert: RequestValidation stops it before the route, with the field
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("name", error.field)
    }

    @Test
    fun `a barcode longer than 255 characters gives 400 and names the field`() = testApplication {
        // Arrange: validBody plus a barcode that does not fit varchar(255) (E-03: was a 500 on INSERT)
        application { testApp() }
        val body = validBody.replace("\"quantity\": 2,", "\"quantity\": 2, \"barcode\": \"${"1".repeat(256)}\",")

        // Act
        val response = place(body = body, token = tokenFor(Role.SUPPLIER))

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("barcode", error.field)
    }

    @Test
    fun `an empty allergen list is allowed`() = testApplication {
        // Arrange: a product without allergens is valid
        application { testApp() }
        val body = """
            {"category": "FRESH", "name": "Volkoren brood", "originalPrice": 3.49,
             "bestBeforeAt": "2026-10-04T00:00:00Z", "quantity": 2,
             "pickupWindow": {"from": "2026-10-01T09:00:00Z", "until": "2026-10-01T17:00:00Z"},
             "allergens": []}
        """

        // Act
        val response = place(body = body, token = tokenFor(Role.SUPPLIER))

        // Assert
        assertEquals(HttpStatusCode.Created, response.status)
        val product = Json.decodeFromString<ProductResponse>(response.bodyAsText())
        assertTrue(product.allergens.isEmpty())
    }

    @Test
    fun `without a pickup window the answer is 422`() = testApplication {
        // Arrange: no pickupWindow, and opening hours (US-10) do not exist yet
        application { testApp() }
        val body = """
            {"category": "FRESH", "name": "Volkoren brood", "originalPrice": 3.49,
             "bestBeforeAt": "2026-10-04T00:00:00Z", "quantity": 2,
             "allergens": ["CEREALS_CONTAINING_GLUTEN"]}
        """

        // Act
        val response = place(body = body, token = tokenFor(Role.SUPPLIER))

        // Assert
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("pickupWindow", error.field)
    }

    @Test
    fun `a best-before date in the past gives 422`() = testApplication {
        // Arrange: best-before one day before testNow; the window still ends before it,
        // so only the expiry rule is broken
        application { testApp() }
        val body = """
            {"category": "FRESH", "name": "Volkoren brood", "originalPrice": 3.49,
             "bestBeforeAt": "2026-09-30T00:00:00Z", "quantity": 2,
             "pickupWindow": {"from": "2026-09-29T09:00:00Z", "until": "2026-09-29T17:00:00Z"},
             "allergens": ["CEREALS_CONTAINING_GLUTEN"]}
        """

        // Act
        val response = place(body = body, token = tokenFor(Role.SUPPLIER))

        // Assert
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("bestBeforeAt", error.field)
    }

    @Test
    fun `a supplier account without a supplier row gets 403`() = testApplication {
        // Arrange: the fake knows a supplier for user 7 only
        application { testApp() }

        // Act: a SUPPLIER token for user 99
        val response = place(token = tokenFor(Role.SUPPLIER, userId = 99))

        // Assert
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `the price in euros is stored in cents`() = testApplication {
        // Arrange: a real database with a supplier, and the Exposed repositories behind the service
        createEmptyTestDatabase(UsersTable, SuppliersTable, ProductsTable, ProductAllergensTable)
        val userId = insertUser(Role.SUPPLIER)
        insertSupplier(userId)
        application {
            testApp(products = ExposedProductRepository(FixedClock(testNow)), suppliers = ExposedSupplierRepository())
        }

        // Act: validBody has originalPrice 3.49
        val response = place(token = tokenFor(Role.SUPPLIER, userId = userId))

        // Assert: the status first, so a failed POST shows as such and not as an empty table
        assertEquals(HttpStatusCode.Created, response.status)
        // Read the column itself, so the test sees what the database holds, not what the API returns.
        val storedCents = transaction { ProductsTable.selectAll().single()[ProductsTable.originalPriceCents] }
        assertEquals(349L, storedCents)
    }
}
