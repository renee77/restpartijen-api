package com.restpartijen.api.product

import com.restpartijen.api.product.client.OpenFoodFactsClient
import com.restpartijen.api.product.service.ProductLookup
import com.restpartijen.api.product.service.ProductService
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.NotFoundException
import com.restpartijen.api.testsupport.FixedClock
import com.restpartijen.api.testsupport.MovableClock
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

/**
 * Unit test: ProductService.lookupProduct with a real client against a MockEngine (§5.2, SD-1).
 * The MockEngine counts the calls, so the tests can see whether the cache answered.
 */
class ProductServiceLookupTest {

    @Test
    fun `lookupProduct returns the name and the EU allergens of a found product`() = runTest {
        // Arrange
        val service = serviceAnswering(HttpStatusCode.OK, NUTELLA_BODY)

        // Act
        val lookup = service.lookupProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(
            ProductLookup("Nutella", setOf(Allergen.MILK, Allergen.NUTS, Allergen.SOYBEANS), manualEntryRequired = false),
            lookup,
        )
    }

    @Test
    fun `lookupProduct throws NotFoundException for a barcode Open Food Facts does not know`() = runTest {
        // Arrange
        val service = serviceAnswering(HttpStatusCode.NotFound, """{"status":"failure"}""")

        // Act + Assert
        assertFailsWith<NotFoundException> { service.lookupProduct(NUTELLA_BARCODE) }
    }

    @Test
    fun `lookupProduct asks for manual entry when Open Food Facts gives no answer`() = runTest {
        // Arrange
        val service = serviceAnswering(HttpStatusCode.TooManyRequests, "")

        // Act
        val lookup = service.lookupProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(ProductLookup(null, emptySet(), manualEntryRequired = true), lookup)
    }

    @Test
    fun `lookupProduct answers a second lookup of a found product from the cache`() = runTest {
        // Arrange
        val calls = CallCounter(HttpStatusCode.OK, NUTELLA_BODY)
        val service = serviceWith(calls.client)
        val first = service.lookupProduct(NUTELLA_BARCODE)

        // Act
        val second = service.lookupProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(first, second)
        assertEquals(1, calls.count)
    }

    @Test
    fun `lookupProduct asks Open Food Facts again after no answer`() = runTest {
        // Arrange
        // A rate limit must not stick: the next lookup tries again instead of repeating the failure.
        val calls = CallCounter(HttpStatusCode.TooManyRequests, "")
        val service = serviceWith(calls.client)
        service.lookupProduct(NUTELLA_BARCODE)

        // Act
        service.lookupProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(2, calls.count)
    }

    @Test
    fun `lookupProduct remembers an unknown barcode for 60 seconds`() = runTest {
        // Arrange
        // A supplier who retries an unknown barcode should not use up the limit of Open Food Facts.
        val calls = CallCounter(HttpStatusCode.NotFound, """{"status":"failure"}""")
        val service = serviceWith(calls.client)
        assertFailsWith<NotFoundException> { service.lookupProduct(NUTELLA_BARCODE) }

        // Act + Assert
        assertFailsWith<NotFoundException> { service.lookupProduct(NUTELLA_BARCODE) }
        assertEquals(1, calls.count)
    }

    @Test
    fun `lookupProduct asks Open Food Facts again for an unknown barcode after 60 seconds`() = runTest {
        // Arrange
        // Someone may add the product to Open Food Facts, so a 404 is remembered only briefly.
        val clock = MovableClock(testNow)
        val calls = CallCounter(HttpStatusCode.NotFound, """{"status":"failure"}""")
        val service = serviceWith(calls.client, clock)
        assertFailsWith<NotFoundException> { service.lookupProduct(NUTELLA_BARCODE) }
        clock.now = testNow + 60.seconds

        // Act + Assert
        assertFailsWith<NotFoundException> { service.lookupProduct(NUTELLA_BARCODE) }
        assertEquals(2, calls.count)
    }

    @Test
    fun `lookupProduct asks for manual entry without calling Open Food Facts after 10 calls in a minute`() = runTest {
        // Arrange
        // 10 different barcodes, so the cache cannot answer; the 11th would pass our share of the limit.
        val calls = CallCounter(HttpStatusCode.OK, NUTELLA_BODY)
        val service = serviceWith(calls.client)
        for (number in 1..10) service.lookupProduct("100000000000$number")

        // Act
        val lookup = service.lookupProduct("2000000000000")

        // Assert
        assertEquals(ProductLookup(null, emptySet(), manualEntryRequired = true), lookup)
        assertEquals(10, calls.count)
    }

    @Test
    fun `lookupProduct does not ask for manual entry when a found product has no name and no allergens`() = runTest {
        // Arrange
        // Open Food Facts answered, so the flag stays false; the app sees the empty fields itself (§5.2).
        val service = serviceAnswering(HttpStatusCode.OK, """{"product":{}}""")

        // Act
        val lookup = service.lookupProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(ProductLookup(null, emptySet(), manualEntryRequired = false), lookup)
    }

    @Test
    fun `lookupProduct leaves the name empty when Open Food Facts has a blank name`() = runTest {
        // Arrange
        val service = serviceAnswering(HttpStatusCode.OK, """{"product":{"product_name":"   ","allergens_tags":["en:milk"]}}""")

        // Act
        val lookup = service.lookupProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(ProductLookup(null, setOf(Allergen.MILK), manualEntryRequired = false), lookup)
    }

    @Test
    fun `lookupProduct keeps a name of 255 characters`() = runTest {
        // Arrange
        // 255 is the most POST and PUT accept, so this name can be placed as it is.
        val name = "a".repeat(255)
        val service = serviceAnswering(HttpStatusCode.OK, """{"product":{"product_name":"$name"}}""")

        // Act
        val lookup = service.lookupProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(name, lookup.name)
    }

    @Test
    fun `lookupProduct leaves the name empty when it is longer than 255 characters`() = runTest {
        // Arrange
        // Cutting it off would look usable but be half a name; empty makes the supplier type it.
        val service = serviceAnswering(HttpStatusCode.OK, """{"product":{"product_name":"${"a".repeat(256)}"}}""")

        // Act
        val lookup = service.lookupProduct(NUTELLA_BARCODE)

        // Assert
        assertEquals(null, lookup.name)
    }

    private fun serviceAnswering(status: HttpStatusCode, body: String) = serviceWith(CallCounter(status, body).client)

    private fun serviceWith(client: OpenFoodFactsClient, clock: Clock = FixedClock(testNow)): ProductService =
        productService(fakeProductRepository(), fakeSupplierRepository(), FixedPriceProvider(), clock, client)

    // Plays Open Food Facts with one fixed answer and counts how often it was asked.
    private class CallCounter(status: HttpStatusCode, body: String) {
        var count = 0
            private set

        val client = OpenFoodFactsClient(MockEngine {
            count++
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        })
    }

    private companion object {
        const val NUTELLA_BARCODE = "3017620422003"

        // Real response of GET /api/v3/product/3017620422003?fields=product_name,allergens_tags, 22 Oct 2026.
        const val NUTELLA_BODY = """{"code":"3017620422003","errors":[],"product":{"allergens_tags":["en:milk","en:nuts","en:soybeans"],"product_name":"Nutella"},"result":{"id":"product_found","lc_name":"Product found","name":"Product found"},"status":"success","warnings":[]}"""
    }
}
