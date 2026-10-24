package com.restpartijen.api.product

import com.restpartijen.api.product.client.OpenFoodFactsClient
import com.restpartijen.api.product.service.ProductLookup
import com.restpartijen.api.product.service.ProductLookupService
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.NotFoundException
import com.restpartijen.api.testsupport.FixedClock
import com.restpartijen.api.testsupport.MovableClock
import com.restpartijen.api.testsupport.NUTELLA_BODY
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Unit test: ProductLookupService with a real client against a MockEngine (§5.2, SD-1).
 * The MockEngine counts the calls, so the tests can see whether a cache or the limit answered.
 */
class ProductLookupServiceTest {

    @Test
    fun `lookup returns the name and the EU allergens of a found product`() = runTest {
        // Arrange
        val service = serviceAnswering(HttpStatusCode.OK, NUTELLA_BODY)

        // Act
        val lookup = service.lookup(NUTELLA_BARCODE, USER)

        // Assert
        assertEquals(nutella, lookup)
    }

    @Test
    fun `lookup throws NotFoundException for a barcode Open Food Facts does not know`() = runTest {
        // Arrange
        val service = serviceAnswering(HttpStatusCode.NotFound, NOT_FOUND_BODY)

        // Act + Assert
        assertFailsWith<NotFoundException> { service.lookup(NUTELLA_BARCODE, USER) }
    }

    @Test
    fun `lookup asks for manual entry when Open Food Facts gives no answer`() = runTest {
        // Arrange
        val service = serviceAnswering(HttpStatusCode.TooManyRequests, "")

        // Act
        val lookup = service.lookup(NUTELLA_BARCODE, USER)

        // Assert
        assertEquals(ProductLookup(null, emptySet(), manualEntryRequired = true), lookup)
    }

    @Test
    fun `lookup answers a second lookup of a found product from the cache`() = runTest {
        // Arrange
        val calls = CallCounter(HttpStatusCode.OK, NUTELLA_BODY)
        val service = serviceWith(calls.client)
        val first = service.lookup(NUTELLA_BARCODE, USER)

        // Act
        val second = service.lookup(NUTELLA_BARCODE, USER)

        // Assert
        assertEquals(first, second)
        assertEquals(1, calls.count)
    }

    @Test
    fun `lookup keeps a found product for 24 hours`() = runTest {
        // Arrange
        val clock = MovableClock(testNow)
        val calls = CallCounter(HttpStatusCode.OK, NUTELLA_BODY)
        val service = serviceWith(calls.client, clock)
        service.lookup(NUTELLA_BARCODE, USER)

        // Act: one minute before the 24 hours, and then exactly at them
        clock.now = testNow + 24.hours - 1.minutes
        service.lookup(NUTELLA_BARCODE, USER)
        val callsJustBefore = calls.count
        clock.now = testNow + 24.hours
        service.lookup(NUTELLA_BARCODE, USER)

        // Assert: still from the cache just before, from Open Food Facts again at 24 hours
        assertEquals(1, callsJustBefore)
        assertEquals(2, calls.count)
    }

    @Test
    fun `lookup still remembers an unknown barcode after 59 seconds`() = runTest {
        // Arrange
        val clock = MovableClock(testNow)
        val calls = CallCounter(HttpStatusCode.NotFound, NOT_FOUND_BODY)
        val service = serviceWith(calls.client, clock)
        assertFailsWith<NotFoundException> { service.lookup(NUTELLA_BARCODE, USER) }
        clock.now = testNow + 59.seconds

        // Act + Assert
        assertFailsWith<NotFoundException> { service.lookup(NUTELLA_BARCODE, USER) }
        assertEquals(1, calls.count)
    }

    @Test
    fun `lookup keeps the cache per barcode`() = runTest {
        // Arrange
        val calls = CallCounter(HttpStatusCode.OK, NUTELLA_BODY)
        val service = serviceWith(calls.client)
        service.lookup(NUTELLA_BARCODE, USER)

        // Act: another barcode is not in the cache yet; the first one is
        service.lookup("8710400000001", USER)
        service.lookup(NUTELLA_BARCODE, USER)

        // Assert
        assertEquals(2, calls.count)
    }

    @Test
    fun `lookup asks Open Food Facts again after no answer`() = runTest {
        // Arrange
        // A rate limit must not stick: the next lookup tries again instead of repeating the failure.
        val calls = CallCounter(HttpStatusCode.TooManyRequests, "")
        val service = serviceWith(calls.client)
        service.lookup(NUTELLA_BARCODE, USER)

        // Act
        service.lookup(NUTELLA_BARCODE, USER)

        // Assert
        assertEquals(2, calls.count)
    }

    @Test
    fun `lookup remembers an unknown barcode for 60 seconds`() = runTest {
        // Arrange
        // A supplier who retries an unknown barcode should not use up the limit of Open Food Facts.
        val calls = CallCounter(HttpStatusCode.NotFound, NOT_FOUND_BODY)
        val service = serviceWith(calls.client)
        assertFailsWith<NotFoundException> { service.lookup(NUTELLA_BARCODE, USER) }

        // Act + Assert
        assertFailsWith<NotFoundException> { service.lookup(NUTELLA_BARCODE, USER) }
        assertEquals(1, calls.count)
    }

    @Test
    fun `lookup asks Open Food Facts again for an unknown barcode after 60 seconds`() = runTest {
        // Arrange
        // Someone may add the product to Open Food Facts, so a 404 is remembered only briefly.
        val clock = MovableClock(testNow)
        val calls = CallCounter(HttpStatusCode.NotFound, NOT_FOUND_BODY)
        val service = serviceWith(calls.client, clock)
        assertFailsWith<NotFoundException> { service.lookup(NUTELLA_BARCODE, USER) }
        clock.now = testNow + 60.seconds

        // Act + Assert
        assertFailsWith<NotFoundException> { service.lookup(NUTELLA_BARCODE, USER) }
        assertEquals(2, calls.count)
    }

    @Test
    fun `lookup asks for manual entry without calling out after 5 calls of one supplier in a minute`() = runTest {
        // Arrange
        // 5 different barcodes, so no cache can answer; the 6th is above the share of one supplier.
        val calls = CallCounter(HttpStatusCode.OK, NUTELLA_BODY)
        val service = serviceWith(calls.client)
        for (number in 1..5) service.lookup("100000000000$number", USER)

        // Act
        val lookup = service.lookup("2000000000000", USER)

        // Assert
        assertEquals(ProductLookup.MANUAL_ENTRY, lookup)
        assertEquals(5, calls.count)
    }

    @Test
    fun `lookup asks for manual entry without calling out after 10 calls in a minute`() = runTest {
        // Arrange
        // Two suppliers with 5 different barcodes each: the 11th call is above the limit of the application.
        val calls = CallCounter(HttpStatusCode.OK, NUTELLA_BODY)
        val service = serviceWith(calls.client)
        for (number in 1..5) service.lookup("100000000000$number", USER)
        for (number in 1..5) service.lookup("300000000000$number", OTHER_USER)

        // Act
        val lookup = service.lookup("2000000000000", THIRD_USER)

        // Assert
        assertEquals(ProductLookup.MANUAL_ENTRY, lookup)
        assertEquals(10, calls.count)
    }

    @Test
    fun `lookup still answers a found product from the cache when the limit is reached`() = runTest {
        // Arrange
        // The cache comes before the limit, so cached answers never cost a call to Open Food Facts.
        val calls = CallCounter(HttpStatusCode.OK, NUTELLA_BODY)
        val service = serviceWith(calls.client)
        service.lookup(NUTELLA_BARCODE, USER)
        for (number in 1..4) service.lookup("100000000000$number", USER)

        // Act
        val lookup = service.lookup(NUTELLA_BARCODE, USER)

        // Assert
        assertEquals(nutella, lookup)
        assertEquals(5, calls.count)
    }

    @Test
    fun `lookup still answers a known unknown barcode with 404 when the limit is reached`() = runTest {
        // Arrange
        val calls = CallCounter(HttpStatusCode.NotFound, NOT_FOUND_BODY)
        val service = serviceWith(calls.client)
        repeat(5) { number -> assertFailsWith<NotFoundException> { service.lookup("100000000000$number", USER) } }

        // Act + Assert: the first barcode is still remembered as unknown, so no manual entry
        assertFailsWith<NotFoundException> { service.lookup("1000000000000", USER) }
        assertEquals(5, calls.count)
    }

    @Test
    fun `lookup does not ask for manual entry when a found product has no name and no allergens`() = runTest {
        // Arrange
        // Open Food Facts answered, so the flag stays false; the app sees the empty fields itself (§5.2).
        val service = serviceAnswering(HttpStatusCode.OK, """{"product":{}}""")

        // Act
        val lookup = service.lookup(NUTELLA_BARCODE, USER)

        // Assert
        assertEquals(ProductLookup(null, emptySet(), manualEntryRequired = false), lookup)
    }

    @Test
    fun `lookup leaves the name empty when Open Food Facts has a blank name`() = runTest {
        // Arrange
        val body = """{"product":{"product_name":"   ","allergens_tags":["en:milk"]}}"""
        val service = serviceAnswering(HttpStatusCode.OK, body)

        // Act
        val lookup = service.lookup(NUTELLA_BARCODE, USER)

        // Assert
        assertEquals(ProductLookup(null, setOf(Allergen.MILK), manualEntryRequired = false), lookup)
    }

    @Test
    fun `lookup keeps a name of 255 characters`() = runTest {
        // Arrange
        // 255 is the most POST and PUT accept, so this name can be placed as it is.
        val name = "a".repeat(255)
        val service = serviceAnswering(HttpStatusCode.OK, """{"product":{"product_name":"$name"}}""")

        // Act
        val lookup = service.lookup(NUTELLA_BARCODE, USER)

        // Assert
        assertEquals(name, lookup.name)
    }

    @Test
    fun `lookup leaves the name empty when it is longer than 255 characters`() = runTest {
        // Arrange
        // Cutting it off would look usable but be half a name; empty makes the supplier type it.
        val service = serviceAnswering(HttpStatusCode.OK, """{"product":{"product_name":"${"a".repeat(256)}"}}""")

        // Act
        val lookup = service.lookup(NUTELLA_BARCODE, USER)

        // Assert
        assertNull(lookup.name)
    }

    /** A service whose Open Food Facts always answers [status] and [body]. */
    private fun serviceAnswering(status: HttpStatusCode, body: String) = serviceWith(CallCounter(status, body).client)

    /** A service on [client], with [clock] for the caches and the limits. */
    private fun serviceWith(client: OpenFoodFactsClient, clock: Clock = FixedClock(testNow)) =
        ProductLookupService(client, clock)

    /**
     * Plays Open Food Facts with one fixed answer and counts how often it was asked.
     *
     * @property count how many calls reached Open Food Facts
     * @property client the OpenFoodFactsClient that counts its calls
     */
    private class CallCounter(status: HttpStatusCode, body: String) {
        var count = 0
            private set

        val client = openFoodFactsClient(
            MockEngine {
                count++
                respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
            },
        )
    }

    private companion object {
        const val NUTELLA_BARCODE = "3017620422003"
        const val USER = 7L
        const val OTHER_USER = 8L
        const val THIRD_USER = 9L

        val nutella = ProductLookup("Nutella", setOf(Allergen.MILK, Allergen.NUTS, Allergen.SOYBEANS), false)

        const val NOT_FOUND_BODY = """{"status":"failure"}"""
    }
}
