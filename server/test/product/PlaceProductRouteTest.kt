package com.restpartijen.api.product

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.plugins.ErrorResponse
import com.restpartijen.api.product.dto.ProductResponse
import com.restpartijen.api.product.model.OpeningHours
import com.restpartijen.api.product.repository.ExposedProductRepository
import com.restpartijen.api.product.repository.ExposedSupplierRepository
import com.restpartijen.api.product.repository.ProductAllergensTable
import com.restpartijen.api.product.repository.ProductRepository
import com.restpartijen.api.product.repository.SupplierOpeningHoursTable
import com.restpartijen.api.product.repository.SupplierRepository
import com.restpartijen.api.shared.Allergen
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
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * POST /api/v1/products through all layers, with fakes behind the service (US-01, §5.1).
 * Only a SUPPLIER may place a product (decision 3.6).
 */
class PlaceProductRouteTest {

    // Fixed during the test (GI-6, decision 6.5), but at the real "now" (#62); see productRoutesTestApp.
    private val tokenClock = FixedClock(Clock.System.now())

    // Fakes by default; the database test passes the Exposed repositories instead.
    private fun Application.testApp(
        products: ProductRepository = fakeProductRepository(),
        suppliers: SupplierRepository = fakeSupplierRepository(aSupplier, ownerUserId = supplierUserId),
    ) = productRoutesTestApp(tokenClock, products, suppliers)

    private fun tokenFor(role: Role, userId: Long = supplierUserId) = productToken(role, tokenClock, userId)

    // A valid body: best-before three days after testNow. No pickup window: the API derives it (US-10).
    private val validBody = """
        {"category": "FRESH", "name": "Volkoren brood", "originalPrice": 3.49,
         "bestBeforeAt": "2026-10-04", "quantity": 2,
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
             "bestBeforeAt": "2026-10-04", "quantity": 2,
             "allergens": ["CHOCOLATE"]}
        """

        // Act
        val response = place(body = body, token = tokenFor(Role.SUPPLIER))

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `a best-before moment instead of a date gives 400`() = testApplication {
        // Arrange: a label shows a date, so the API only accepts a date (decision 23 Oct 2026)
        application { testApp() }
        val body = validBody.replace("\"2026-10-04\"", "\"2026-10-04T00:00:00Z\"")

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
    fun `a barcode of more than 14 digits gives 400 and names the field`() = testApplication {
        // Arrange: validBody plus a barcode of 15 digits; one more than GTIN-14 allows (US-02).
        // Before US-02 this test used 256 characters for the varchar(255) limit (E-03).
        application { testApp() }
        val body = validBody.replace("\"quantity\": 2,", "\"quantity\": 2, \"barcode\": \"${"1".repeat(15)}\",")

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
             "bestBeforeAt": "2026-10-04", "quantity": 2,
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
    fun `the API derives the pickup window from the opening hours`() = testApplication {
        // Arrange: the fake supplier is open every day 08:00-21:00 (US-10)
        application { testApp() }

        // Act
        val response = place(token = tokenFor(Role.SUPPLIER))

        // Assert: from Thu 1 Oct 08:00 to Sun 4 Oct 21:00 in Amsterdam; the date counts up to Mon 5 Oct 00:00
        assertEquals(HttpStatusCode.Created, response.status)
        val window = Json.decodeFromString<ProductResponse>(response.bodyAsText()).pickupWindow
        assertEquals("2026-10-01T06:00:00Z", window.from)
        assertEquals("2026-10-04T19:00:00Z", window.until)
    }

    @Test
    fun `a body with a pickupWindow gives 400, because the API derives the window`() = testApplication {
        // Arrange: the field no longer exists (US-10); the JSON reader refuses an unknown field (B-49)
        application { testApp() }
        val window = """"pickupWindow": {"from": "2026-10-01T09:00:00Z", "until": "2026-10-01T17:00:00Z"},"""
        val body = validBody.replace("\"quantity\": 2,", "\"quantity\": 2, $window")

        // Act
        val response = place(body = body, token = tokenFor(Role.SUPPLIER))

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `a supplier that is not open before bestBeforeAt gives 422`() = testApplication {
        // Arrange: open on Saturday only, and the product expires on Thursday 1 October (§5.10)
        val saturday = OpeningHours(DayOfWeek.SATURDAY, opensAt = LocalTime(9, 0), closesAt = LocalTime(18, 0))
        application { testApp(suppliers = fakeSupplierRepository(aSupplier, supplierUserId, listOf(saturday))) }
        val body = validBody.replace("\"2026-10-04\"", "\"2026-10-01\"")

        // Act
        val response = place(body = body, token = tokenFor(Role.SUPPLIER))

        // Assert
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("bestBeforeAt", error.field)
    }

    @Test
    fun `a best-before date in the past gives 422`() = testApplication {
        // Arrange: best-before one day before testNow, so there is no time left for a window
        application { testApp() }
        val body = """
            {"category": "FRESH", "name": "Volkoren brood", "originalPrice": 3.49,
             "bestBeforeAt": "2026-09-30", "quantity": 2,
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
        createEmptyTestDatabase(
            UsersTable,
            SuppliersTable,
            ProductsTable,
            ProductAllergensTable,
            SupplierOpeningHoursTable,
        )
        val userId = insertUser(Role.SUPPLIER)
        // A supplier always has opening hours (US-10); without them the product gets no pickup window.
        ExposedSupplierRepository().updateOpeningHours(insertSupplier(userId), anOpenWeek)
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
