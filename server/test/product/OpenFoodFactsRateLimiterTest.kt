package com.restpartijen.api.product

import com.restpartijen.api.product.service.OpenFoodFactsRateLimiter
import com.restpartijen.api.testsupport.MovableClock
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Unit test: at most 10 calls to Open Food Facts per minute, under its limit of 15 per IP (§10.3, decision Eva 22 Oct).
 */
class OpenFoodFactsRateLimiterTest {

    private val clock = MovableClock(testNow)
    private val limiter = OpenFoodFactsRateLimiter(clock)

    @Test
    fun `tryAcquire allows 10 calls within a minute`() {
        // Act + Assert
        repeat(10) { assertTrue(limiter.tryAcquire(), "call ${it + 1}") }
    }

    @Test
    fun `tryAcquire refuses the 11th call within a minute`() {
        // Arrange
        repeat(10) { limiter.tryAcquire() }

        // Act + Assert
        assertFalse(limiter.tryAcquire())
    }

    @Test
    fun `tryAcquire allows a call again once the oldest is a minute old`() {
        // Arrange
        repeat(10) { limiter.tryAcquire() }
        clock.now = testNow + 1.minutes

        // Act + Assert
        assertTrue(limiter.tryAcquire())
    }

    @Test
    fun `tryAcquire still refuses just before the oldest is a minute old`() {
        // Arrange
        repeat(10) { limiter.tryAcquire() }
        clock.now = testNow + 1.minutes - 1.seconds

        // Act + Assert
        assertFalse(limiter.tryAcquire())
    }

    @Test
    fun `a refused call does not count`() {
        // Arrange
        // Otherwise a supplier who keeps trying would keep the limit closed for everyone.
        repeat(10) { limiter.tryAcquire() }
        clock.now = testNow + 30.seconds
        repeat(10) { limiter.tryAcquire() }
        clock.now = testNow + 1.minutes

        // Act + Assert: the 10 refused calls at 30 seconds left no trace, so 10 new calls fit
        repeat(10) { assertTrue(limiter.tryAcquire(), "call ${it + 1}") }
    }
}
