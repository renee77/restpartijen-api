package com.restpartijen.api.product

import com.restpartijen.api.plugins.ErrorCode
import com.restpartijen.api.plugins.ErrorResponse
import com.restpartijen.api.product.client.OpenFoodFactsClient
import com.restpartijen.api.product.dto.ProductLookupResponse
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.FixedClock
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock

/**
 * GET /api/v1/products/lookup/{barcode} through all layers, with Open Food Facts played by a MockEngine (US-02, §5.2).
 * Order of the checks: role (401/403), barcode shape (400); then 200, 404, or 200 with manualEntryRequired.
 */
class LookupProductRouteTest {

    // Fixed during the test (GI-6, decision 6.5), but at the real "now" (#62); see productRoutesTestApp.
    private val tokenClock = FixedClock(Clock.System.now())

    private fun tokenFor(role: Role) = productToken(role, tokenClock)

    private suspend fun ApplicationTestBuilder.lookup(
        barcode: String,
        token: String? = tokenFor(Role.SUPPLIER),
    ): HttpResponse =
        client.get("/api/v1/products/lookup/$barcode") { token?.let { bearerAuth(it) } }

    private fun ApplicationTestBuilder.appWithOpenFoodFacts(status: HttpStatusCode, body: String) = application {
        productRoutesTestApp(
            tokenClock,
            openFoodFacts = openFoodFactsClient(MockEngine {
                respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
            }),
        )
    }

    @Test
    fun `a supplier gets the name, the allergens and no manual entry for a known barcode`() = testApplication {
        // Arrange
        appWithOpenFoodFacts(HttpStatusCode.OK, NUTELLA_BODY)

        // Act
        val response = lookup(NUTELLA_BARCODE)

        // Assert: the JSON field names are the contract with the app; the order of the allergens is not
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertEquals(setOf("name", "allergens", "manualEntryRequired"), Json.parseToJsonElement(body).jsonObject.keys)
        assertEquals(
            ProductLookupResponse("Nutella", setOf(Allergen.MILK, Allergen.NUTS, Allergen.SOYBEANS), false),
            Json.decodeFromString<ProductLookupResponse>(body),
        )
    }

    @Test
    fun `a supplier gets 200 with manual entry when Open Food Facts gives no answer`() = testApplication {
        // Arrange
        appWithOpenFoodFacts(HttpStatusCode.ServiceUnavailable, "")

        // Act
        val response = lookup(NUTELLA_BARCODE)

        // Assert: placing a product stays possible, the supplier fills in everything (§5.2)
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            ProductLookupResponse(null, emptySet(), manualEntryRequired = true),
            Json.decodeFromString<ProductLookupResponse>(response.bodyAsText()),
        )
    }

    @Test
    fun `an unknown barcode gives 404 with the error body`() = testApplication {
        // Arrange
        appWithOpenFoodFacts(HttpStatusCode.NotFound, """{"status":"failure"}""")

        // Act
        val response = lookup("8712345678906")

        // Assert: the error body is part of the contract with the app, like for every other 404 (§10.2)
        assertEquals(HttpStatusCode.NotFound, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals(ErrorCode.NOT_FOUND, error.code)
        assertEquals("No product found for this barcode", error.message)
        assertNull(error.field)
    }

    @Test
    fun `a collector with a malformed barcode gets 403, not 400`() = testApplication {
        // Arrange: the role is checked before the barcode, so a collector learns nothing about the barcode rule
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = lookup("abc", token = tokenFor(Role.COLLECTOR))

        // Assert
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `a lookup without a token gives 401`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = lookup(NUTELLA_BARCODE, token = null)

        // Assert
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `a collector gets 403`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = lookup(NUTELLA_BARCODE, token = tokenFor(Role.COLLECTOR))

        // Assert
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `an admin gets 403`() = testApplication {
        // Arrange: the lookup is for placing a product, which only a supplier does (§5.2)
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = lookup(NUTELLA_BARCODE, token = tokenFor(Role.ADMIN))

        // Assert
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `a barcode with letters gives 400 and names the field, without calling Open Food Facts`() = testApplication {
        // Arrange: openFoodFactsNotCalled (the default) fails the test if the route still calls out
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = lookup("abc1234")

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("barcode", Json.decodeFromString<ErrorResponse>(response.bodyAsText()).field)
    }

    @Test
    fun `a barcode of 6 digits gives 400`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = lookup("123456")

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `a barcode of 15 digits gives 400`() = testApplication {
        // Arrange
        application { productRoutesTestApp(tokenClock) }

        // Act
        val response = lookup("1".repeat(15))

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `barcodes of 7 and 14 digits are accepted`() = testApplication {
        // Arrange: UPC-E has 7 digits, GTIN-14 has 14 (Open Food Facts, barcode normalization)
        appWithOpenFoodFacts(HttpStatusCode.OK, NUTELLA_BODY)

        // Act
        val shortest = lookup("1".repeat(7))
        val longest = lookup("1".repeat(14))

        // Assert
        assertEquals(HttpStatusCode.OK, shortest.status)
        assertEquals(HttpStatusCode.OK, longest.status)
    }

    private companion object {
        const val NUTELLA_BARCODE = "3017620422003"

        // Real response of GET /api/v3/product/3017620422003?fields=product_name,allergens_tags, 22 Oct 2026.
        const val NUTELLA_BODY = """{"code":"3017620422003","errors":[],"product":{"allergens_tags":["en:milk","en:nuts","en:soybeans"],"product_name":"Nutella"},"result":{"id":"product_found","lc_name":"Product found","name":"Product found"},"status":"success","warnings":[]}"""
    }
}
