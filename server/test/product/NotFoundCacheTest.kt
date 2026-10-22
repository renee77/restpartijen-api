package com.restpartijen.api.product

import com.restpartijen.api.product.service.NotFoundCache
import com.restpartijen.api.testsupport.MovableClock
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Unit test: NotFoundCache remembers a barcode Open Food Facts did not know for 60 seconds, at most 1,000 (decision Eva 22 Oct).
 */
class NotFoundCacheTest {

    private val clock = MovableClock(testNow)
    private val cache = NotFoundCache(clock)

    @Test
    fun `contains is true for a barcode just remembered`() {
        // Arrange
        cache.remember(UNKNOWN_BARCODE)

        // Act + Assert
        assertTrue(cache.contains(UNKNOWN_BARCODE))
    }

    @Test
    fun `contains is false for a barcode never remembered`() {
        assertFalse(cache.contains(UNKNOWN_BARCODE))
    }

    @Test
    fun `contains is still true just before 60 seconds`() {
        // Arrange
        cache.remember(UNKNOWN_BARCODE)
        clock.now = testNow + 59.seconds

        // Act + Assert
        assertTrue(cache.contains(UNKNOWN_BARCODE))
    }

    @Test
    fun `contains is false once 60 seconds have passed`() {
        // Arrange
        // Short, because someone may add the product to Open Food Facts.
        cache.remember(UNKNOWN_BARCODE)
        clock.now = testNow + 60.seconds

        // Act + Assert
        assertFalse(cache.contains(UNKNOWN_BARCODE))
    }

    @Test
    fun `remember drops the oldest barcode when 1000 barcodes are remembered`() {
        // Arrange
        for (number in 1..1_000) cache.remember(number.toString())

        // Act
        cache.remember("1001")

        // Assert
        assertFalse(cache.contains("1"))
        assertTrue(cache.contains("2"))
        assertTrue(cache.contains("1001"))
    }

    private companion object {
        const val UNKNOWN_BARCODE = "8712345678906"
    }
}
