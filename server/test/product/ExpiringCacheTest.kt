package com.restpartijen.api.product

import com.restpartijen.api.product.service.ExpiringCache
import com.restpartijen.api.testsupport.MovableClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Unit test: ExpiringCache, the one mechanism behind both lookup caches (24 h for found products,
 * 60 s for unknown barcodes): a time to live, at most a fixed number of keys, the oldest out first.
 */
class ExpiringCacheTest {

    private val clock = MovableClock(testNow)
    private val cache = ExpiringCache<String>(clock, timeToLive = 24.hours, maxEntries = 1_000)

    @Test
    fun `get returns what was put for the key`() {
        // Arrange
        cache.put(KEY, VALUE)

        // Act + Assert
        assertEquals(VALUE, cache.get(KEY))
    }

    @Test
    fun `get returns null for a key that was never put`() {
        assertNull(cache.get(KEY))
    }

    @Test
    fun `get returns null for another key than the one put`() {
        // Arrange
        cache.put(KEY, VALUE)

        // Act + Assert
        assertNull(cache.get("8712345678906"))
    }

    @Test
    fun `get still returns the value just before the time to live`() {
        // Arrange
        cache.put(KEY, VALUE)
        clock.now = testNow + 24.hours - 1.minutes

        // Act + Assert
        assertEquals(VALUE, cache.get(KEY))
    }

    @Test
    fun `get returns null once the time to live has passed`() {
        // Arrange
        cache.put(KEY, VALUE)
        clock.now = testNow + 24.hours

        // Act + Assert
        assertNull(cache.get(KEY))
    }

    @Test
    fun `put again for the same key restarts its time to live`() {
        // Arrange
        cache.put(KEY, VALUE)
        clock.now = testNow + 23.hours
        cache.put(KEY, VALUE)
        clock.now = testNow + 24.hours

        // Act: 24 hours after the first put, but only 1 hour after the second
        val cached = cache.get(KEY)

        // Assert
        assertEquals(VALUE, cached)
    }

    @Test
    fun `put drops the oldest key when the cache is full`() {
        // Arrange
        // Keys 1 to 1000 fill the cache; key 1001 pushes out key 1, the oldest.
        for (number in 1..1_000) cache.put(number.toString(), VALUE)

        // Act
        cache.put("1001", VALUE)

        // Assert
        assertNull(cache.get("1"))
        assertEquals(VALUE, cache.get("2"))
        assertEquals(VALUE, cache.get("1001"))
    }

    @Test
    fun `put again for a stored key in a full cache drops nothing else`() {
        // Arrange
        for (number in 1..1_000) cache.put(number.toString(), VALUE)

        // Act: key 500 is already there, so the cache does not grow
        cache.put("500", VALUE)

        // Assert
        assertEquals(VALUE, cache.get("1"))
        assertEquals(VALUE, cache.get("500"))
    }

    @Test
    fun `a key put again counts as the newest`() {
        // Arrange
        // Key 1 is stored again after the others, so key 2 is now the oldest and goes out first.
        for (number in 1..1_000) cache.put(number.toString(), VALUE)
        cache.put("1", VALUE)

        // Act
        cache.put("1001", VALUE)

        // Assert
        assertEquals(VALUE, cache.get("1"))
        assertNull(cache.get("2"))
    }

    private companion object {
        const val KEY = "3017620422003"
        const val VALUE = "Nutella"
    }
}
