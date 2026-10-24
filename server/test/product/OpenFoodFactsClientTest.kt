package com.restpartijen.api.product

import com.restpartijen.api.product.client.LookupResult
import com.restpartijen.api.product.client.OpenFoodFactsClient
import com.restpartijen.api.testsupport.NUTELLA_BODY
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ClosedByteChannelException
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import java.io.IOException
import java.nio.channels.UnresolvedAddressException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
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
    fun `fetchProduct calls the v3 product endpoint over https`() = runTest {
        // Act
        val request = requestSentFor(NUTELLA_BARCODE)

        // Assert
        assertEquals("https", request.url.protocol.name)
        assertEquals("world.openfoodfacts.org", request.url.host)
        assertEquals("/api/v3/product/$NUTELLA_BARCODE", request.url.encodedPath)
    }

    @Test
    fun `fetchProduct asks only for the name and the allergen tags`() = runTest {
        // Act
        val request = requestSentFor(NUTELLA_BARCODE)

        // Assert
        assertEquals("product_name,allergens_tags", request.url.parameters["fields"])
    }

    @Test
    fun `fetchProduct names the app, a version and a contact address in the User-Agent`() = runTest {
        // Act
        val request = requestSentFor(NUTELLA_BARCODE)

        // Assert: the format Open Food Facts asks for, AppName/Version (ContactEmail) (A5)
        val userAgent = request.headers[HttpHeaders.UserAgent].orEmpty()
        assertTrue(Regex("""restpartijen-api/\d+\.\d+ \(\S+@\S+\)""").matches(userAgent), userAgent)
    }

    @Test
    fun `fetchProduct returns no tags when Open Food Facts sends null for the allergens`() = runTest {
        // Arrange
        // An explicit null must not cost the supplier the name, as a broken body would.
        val body = """{"product":{"product_name":"Nutella","allergens_tags":null}}"""
        val client = clientResponding(HttpStatusCode.OK, body)

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Found("Nutella", emptyList()), result)
    }

    @Test
    fun `fetchProduct returns Unavailable when the body is larger than 64 KiB`() = runTest {
        // Arrange
        // The two fields we ask for are a few hundred bytes; anything this big is not a normal answer.
        val name = "a".repeat(64 * 1024)
        val client = clientResponding(HttpStatusCode.OK, """{"product":{"product_name":"$name"}}""")

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Unavailable, result)
    }

    @Test
    fun `fetchProduct reads a body of exactly 64 KiB`() = runTest {
        // Arrange: a body of exactly the limit, which must still be read
        val prefix = """{"product":{"product_name":""""
        val suffix = """"}}"""
        val name = "a".repeat(64 * 1024 - prefix.length - suffix.length)
        val client = clientResponding(HttpStatusCode.OK, prefix + name + suffix)

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Found(name, emptyList()), result)
    }

    @Test
    fun `fetchProduct stops reading a body after 64 KiB`() = runTest {
        // Arrange
        // A body of 10 MB, written as it is read. The client must stop after 64 KiB instead of first loading
        // everything into memory, which is what a plain get() does (code and security review, round 2).
        val chunk = ByteArray(8 * 1024) { 'a'.code.toByte() }
        var written = 0L
        val body = ByteChannel()
        backgroundScope.launch {
            try {
                body.writeFully("""{"product":{"product_name":"""".encodeToByteArray())
                repeat(1_280) {
                    body.writeFully(chunk)
                    body.flush()
                    written += chunk.size
                }
                body.close()
            } catch (e: ClosedByteChannelException) {
                // Expected: the client hung up after 64 KiB, which is what this test wants to see. The parent class,
                // because the hang-up can land in writeFully (ClosedWriteChannelException) or in flush
                // (review round 3).
            }
        }
        val client = openFoodFactsClient(
            MockEngine {
                respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            },
        )

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert: Unavailable, and far less than the 10 MiB was produced. Measured on 22 Oct: a plain get() takes
        // all 10 MiB; streaming stops at about 2 MiB, which the channels in between hold before the writer waits.
        assertEquals(LookupResult.Unavailable, result)
        assertTrue(written < 5_000_000, "written: $written bytes")
    }

    @Test
    fun `fetchProduct reads a name with accents as UTF-8`() = runTest {
        // Arrange: Dutch and French names, and a sign of three bytes in UTF-8
        val client = clientResponding(HttpStatusCode.OK, """{"product":{"product_name":"Crème brûlée €"}}""")

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Found("Crème brûlée €", emptyList()), result)
    }

    @Test
    fun `fetchProduct counts the limit in bytes, not in characters`() = runTest {
        // Arrange: 40,000 times "é" is 40,000 characters but 80,000 bytes, so above the 64 KiB
        val client = clientResponding(HttpStatusCode.OK, """{"product":{"product_name":"${"é".repeat(40_000)}"}}""")

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Unavailable, result)
    }

    @Test
    fun `fetchProduct returns Unavailable on a redirect instead of following it`() = runTest {
        // Arrange
        // The API answers directly; a redirect could send the call to another host.
        val requests = mutableListOf<HttpRequestData>()
        val client = openFoodFactsClient(
            MockEngine { request ->
                requests += request
                respond("", HttpStatusCode.Found, headersOf(HttpHeaders.Location, "https://example.org/elsewhere"))
            },
        )

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Unavailable, result)
        assertEquals(1, requests.size)
    }

    @Test
    fun `fetchProduct keeps a strange barcode inside one path segment`() = runTest {
        // Arrange
        // The route allows only digits, but the client must not rely on that: a slash or question mark
        // in the barcode may never add a path segment or a query parameter.
        val requests = mutableListOf<HttpRequestData>()
        val client = openFoodFactsClient(
            MockEngine { request ->
                requests += request
                respondJson(HttpStatusCode.NotFound, NOT_FOUND_BODY)
            },
        )

        // Act
        client.fetchProduct("1234567/../admin?x=1")

        // Assert
        val url = requests.single().url
        assertEquals("/api/v3/product/1234567%2F..%2Fadmin%3Fx=1", url.encodedPath)
        assertNull(url.parameters["x"])
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
        val client = openFoodFactsClient(MockEngine { throw IOException("Connection refused") })

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Unavailable, result)
    }

    @Test
    fun `fetchProduct returns Unavailable when the host name cannot be resolved`() = runTest {
        // Arrange
        // What CIO throws without DNS, for example during an offline demo; it is no IOException.
        val client = openFoodFactsClient(MockEngine { throw UnresolvedAddressException() })

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Unavailable, result)
    }

    @Test
    fun `fetchProduct returns Unavailable when Open Food Facts answers too late`() = runTest {
        // Arrange
        // Longer than the 3 second time-out of the client, so the client has to give up first.
        val client = openFoodFactsClient(
            MockEngine {
                delay(10.seconds)
                respondJson(HttpStatusCode.OK, NUTELLA_BODY)
            },
        )

        // Act
        val result = client.fetchProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(LookupResult.Unavailable, result)
    }

    private fun clientResponding(status: HttpStatusCode, body: String) =
        openFoodFactsClient(MockEngine { respondJson(status, body) })

    // The one request the client sends for [barcode], answered with the real Nutella body.
    private suspend fun requestSentFor(barcode: String): HttpRequestData {
        val requests = mutableListOf<HttpRequestData>()
        val client = openFoodFactsClient(
            MockEngine { request ->
                requests += request
                respondJson(HttpStatusCode.OK, NUTELLA_BODY)
            },
        )
        client.fetchProduct(barcode)
        return requests.single()
    }

    private fun MockRequestHandleScope.respondJson(status: HttpStatusCode, body: String): HttpResponseData = respond(
        content = body,
        status = status,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )

    private companion object {
        const val NUTELLA_BARCODE = "3017620422003"
        const val UNKNOWN_BARCODE = "8712345678906"

        // Real response of GET /api/v3/product/8712345678906?fields=product_name,allergens_tags.
        const val NOT_FOUND_BODY =
            """{"code":"8712345678906","errors":[""" +
                """{"field":{"id":"code","value":"8712345678906"},"impact":""" +
                """{"id":"failure","lc_name":"Failure","name":"Failure"},"message":""" +
                """{"id":"product_not_found","lc_name":"","name":""}}],"result":""" +
                """{"id":"product_not_found","lc_name":"Product not found","name":"Product not found"}""" +
                // The rest has no { or [ to split before and does not fit on the line above.
                ""","status":"failure","warnings":[]}"""
    }
}
