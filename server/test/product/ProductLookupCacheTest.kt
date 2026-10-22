package com.restpartijen.api.product

import com.restpartijen.api.product.service.ProductLookup
import com.restpartijen.api.product.service.ProductLookupCache
import com.restpartijen.api.shared.Allergen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Unit test: ProductLookupCache keeps a found product for 24 hours and at most 1,000 barcodes (§10.3, decision Eva 22 Oct).
 */
class ProductLookupCacheTest {

    private val clock = MovableClock(testNow)
    private val cache = ProductLookupCache(clock)

    @Test
    fun `get returns what was put for the barcode`() {
        // Arrange
        cache.put(NUTELLA_BARCODE, nutella)

        // Act
        val cached = cache.get(NUTELLA_BARCODE)

        // Assert
        assertEquals(nutella, cached)
    }

    @Test
    fun `get returns null for a barcode that was never put`() {
        // Act
        val cached = cache.get(NUTELLA_BARCODE)

        // Assert
        assertNull(cached)
    }

    @Test
    fun `get still returns the product just before 24 hours`() {
        // Arrange
        cache.put(NUTELLA_BARCODE, nutella)
        clock.now = testNow + 24.hours - 1.minutes

        // Act
        val cached = cache.get(NUTELLA_BARCODE)

        // Assert
        assertEquals(nutella, cached)
    }

    @Test
    fun `get returns null once 24 hours have passed`() {
        // Arrange
        cache.put(NUTELLA_BARCODE, nutella)
        clock.now = testNow + 24.hours

        // Act
        val cached = cache.get(NUTELLA_BARCODE)

        // Assert
        assertNull(cached)
    }

    @Test
    fun `put drops the oldest barcode when 1000 barcodes are stored`() {
        // Arrange
        // Barcodes 1 to 1000 fill the cache; barcode 1001 pushes out barcode 1, the oldest.
        for (number in 1..1_000) cache.put(number.toString(), nutella)

        // Act
        cache.put("1001", nutella)

        // Assert
        assertNull(cache.get("1"))
        assertEquals(nutella, cache.get("2"))
        assertEquals(nutella, cache.get("1001"))
    }

    // A clock the test can move forward, to pass the 24 hour limit without waiting.
    private class MovableClock(var now: Instant) : Clock {
        override fun now() = now
    }

    private companion object {
        const val NUTELLA_BARCODE = "3017620422003"

        val nutella = ProductLookup(
            name = "Nutella",
            allergens = setOf(Allergen.MILK, Allergen.NUTS, Allergen.SOYBEANS),
            manualEntryRequired = false,
        )
    }
}
