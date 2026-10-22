package com.restpartijen.api.product

import com.restpartijen.api.product.client.LookupResult
import com.restpartijen.api.product.client.OpenFoodFactsClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import java.io.IOException
import java.nio.channels.UnresolvedAddressException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/**
 * Unit test: OpenFoodFactsClient against a MockEngine that plays the Open Food Facts API v3 (§10.3, A5).
 * The response bodies are copied from real curl calls on 22 Oct 2026, so the tests follow what the API sends.
 */
class OpenFoodFactsClientTest {

    @Test
    fun `fetchProduct returns the name and the tags of a found product`() = runTest {
        // Arrange
        val client = clientResponding(HttpStatusCode.OK, NUTELLA_BODY)

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Found("Nutella", listOf("en:milk", "en:nuts", "en:soybeans")), result)
    }

    @Test
    fun `fetchProduct asks only for the needed fields and names our app in the User-Agent`() = runTest {
        // Arrange
        val requests = mutableListOf<HttpRequestData>()
        val client = OpenFoodFactsClient(MockEngine { request ->
            requests += request
            respondJson(HttpStatusCode.OK, NUTELLA_BODY)
        })

        // Act
        client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        val url = requests.single().url
        assertEquals("https", url.protocol.name)
        assertEquals("world.openfoodfacts.org", url.host)
        assertEquals("/api/v3/product/$NUTELLA_BARCODE", url.encodedPath)
        assertEquals("product_name,allergens_tags", url.parameters["fields"])
        assertEquals(
            "restpartijen-api/0.1 (etm.bouwman@student.avans.nl)",
            requests.single().headers[HttpHeaders.UserAgent],
        )
    }

    @Test
    fun `fetchProduct returns NotFound when Open Food Facts answers 404`() = runTest {
        // Arrange
        val client = clientResponding(HttpStatusCode.NotFound, NOT_FOUND_BODY)

        // Act
        val result = client.fetchProduct(UNKNOWN_BARCODE)

        // Assert
        assertEquals(LookupResult.NotFound, result)
    }

    @Test
    fun `fetchProduct returns a null name when the product has no name`() = runTest {
        // Arrange
        val client = clientResponding(HttpStatusCode.OK, """{"product":{"allergens_tags":["en:milk"]}}""")

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Found(null, listOf("en:milk")), result)
    }

    @Test
    fun `fetchProduct returns no tags when the product has no allergens field`() = runTest {
        // Arrange
        val client = clientResponding(HttpStatusCode.OK, """{"product":{"product_name":"Nutella"}}""")

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Found("Nutella", emptyList()), result)
    }

    @Test
    fun `fetchProduct returns Unavailable when a 200 has no product`() = runTest {
        // Arrange
        val client = clientResponding(HttpStatusCode.OK, """{"status":"success"}""")

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Unavailable, result)
    }

    @Test
    fun `fetchProduct returns Unavailable when the body is not valid JSON`() = runTest {
        // Arrange
        val client = clientResponding(HttpStatusCode.OK, """{"product":""")

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Unavailable, result)
    }

    @Test
    fun `fetchProduct returns Unavailable when Open Food Facts limits the rate`() = runTest {
        // Arrange
        val client = clientResponding(HttpStatusCode.TooManyRequests, "")

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Unavailable, result)
    }

    @Test
    fun `fetchProduct returns Unavailable when Open Food Facts has a server error`() = runTest {
        // Arrange
        val client = clientResponding(HttpStatusCode.InternalServerError, "")

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Unavailable, result)
    }

    @Test
    fun `fetchProduct returns Unavailable when there is no connection`() = runTest {
        // Arrange
        val client = OpenFoodFactsClient(MockEngine { throw IOException("Connection refused") })

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Unavailable, result)
    }

    @Test
    fun `fetchProduct returns Unavailable when the host name cannot be resolved`() = runTest {
        // Arrange
        // What CIO throws without DNS, for example during an offline demo; it is no IOException.
        val client = OpenFoodFactsClient(MockEngine { throw UnresolvedAddressException() })

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Unavailable, result)
    }

    @Test
    fun `fetchProduct returns Unavailable when Open Food Facts answers too late`() = runTest {
        // Arrange
        // Longer than the 3 second time-out of the client, so the client has to give up first.
        val client = OpenFoodFactsClient(MockEngine {
            delay(10.seconds)
            respondJson(HttpStatusCode.OK, NUTELLA_BODY)
        })

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Unavailable, result)
    }

    private fun clientResponding(status: HttpStatusCode, body: String) =
        OpenFoodFactsClient(MockEngine { respondJson(status, body) })

    private fun MockRequestHandleScope.respondJson(status: HttpStatusCode, body: String): HttpResponseData =
        respond(
            content = body,
            status = status,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )

    private companion object {
        const val NUTELLA_BARCODE = "3017620422003"
        const val UNKNOWN_BARCODE = "8712345678906"

        // Real response of GET /api/v3/product/3017620422003?fields=product_name,allergens_tags.
        const val NUTELLA_BODY = """{"code":"3017620422003","errors":[],"product":{"allergens_tags":["en:milk","en:nuts","en:soybeans"],"product_name":"Nutella"},"result":{"id":"product_found","lc_name":"Product found","name":"Product found"},"status":"success","warnings":[]}"""

        // Real response of GET /api/v3/product/8712345678906?fields=product_name,allergens_tags.
        const val NOT_FOUND_BODY = """{"code":"8712345678906","errors":[{"field":{"id":"code","value":"8712345678906"},"impact":{"id":"failure","lc_name":"Failure","name":"Failure"},"message":{"id":"product_not_found","lc_name":"","name":""}}],"result":{"id":"product_not_found","lc_name":"Product not found","name":"Product not found"},"status":"failure","warnings":[]}"""
    }
}
