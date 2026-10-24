package com.restpartijen.api.product

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.restpartijen.api.product.service.OpenFoodFactsRateLimiter
import com.restpartijen.api.testsupport.MovableClock
import org.slf4j.LoggerFactory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Unit test: at most 10 calls to Open Food Facts per minute for the whole application, and at most 5 per supplier,
 * under its limit of 15 per IP (§10.3, decisions Eva 22 Oct; security review F-01 and M-2).
 */
class OpenFoodFactsRateLimiterTest {

    private val clock = MovableClock(testNow)
    private val limiter = OpenFoodFactsRateLimiter(clock)

    @Test
    fun `tryAcquire allows 5 calls of one supplier within a minute`() {
        repeat(5) { assertTrue(limiter.tryAcquire(USER_A), "call ${it + 1}") }
    }

    @Test
    fun `tryAcquire refuses the 6th call of one supplier but still allows another supplier`() {
        // Arrange
        repeat(5) { limiter.tryAcquire(USER_A) }

        // Act + Assert: one supplier cannot use up the limit for everybody
        assertFalse(limiter.tryAcquire(USER_A))
        assertTrue(limiter.tryAcquire(USER_B))
    }

    @Test
    fun `tryAcquire refuses the 11th call within a minute, whoever asks`() {
        // Arrange
        repeat(5) { limiter.tryAcquire(USER_A) }
        repeat(5) { limiter.tryAcquire(USER_B) }

        // Act + Assert
        assertFalse(limiter.tryAcquire(USER_C))
    }

    @Test
    fun `tryAcquire allows calls again once the oldest are a minute old`() {
        // Arrange
        repeat(5) { limiter.tryAcquire(USER_A) }
        repeat(5) { limiter.tryAcquire(USER_B) }
        clock.now = testNow + 1.minutes

        // Act + Assert
        assertTrue(limiter.tryAcquire(USER_A))
    }

    @Test
    fun `tryAcquire still refuses just before the oldest are a minute old`() {
        // Arrange
        repeat(5) { limiter.tryAcquire(USER_A) }
        repeat(5) { limiter.tryAcquire(USER_B) }
        clock.now = testNow + 1.minutes - 1.seconds

        // Act + Assert
        assertFalse(limiter.tryAcquire(USER_C))
    }

    @Test
    fun `tryAcquire counts over a sliding minute, not per clock minute`() {
        // Arrange
        // 5 calls at 0 s and 5 at 30 s. At 60 s only the first 5 have left the window, so 5 more fit and no 6th:
        // a window that empties all at once would allow 20 calls around its edge, above the 15 of Open Food Facts.
        repeat(5) { limiter.tryAcquire(USER_A) }
        clock.now = testNow + 30.seconds
        repeat(5) { limiter.tryAcquire(USER_B) }
        clock.now = testNow + 1.minutes

        // Act + Assert
        repeat(5) { assertTrue(limiter.tryAcquire(USER_C), "call ${it + 1}") }
        assertFalse(limiter.tryAcquire(USER_D))
    }

    @Test
    fun `a refused call does not count`() {
        // Arrange
        // Otherwise a supplier who keeps trying would keep the limit closed.
        repeat(5) { limiter.tryAcquire(USER_A) }
        repeat(5) { limiter.tryAcquire(USER_B) }
        clock.now = testNow + 30.seconds
        repeat(10) { limiter.tryAcquire(USER_C) }
        clock.now = testNow + 1.minutes

        // Act + Assert: the refused calls at 30 seconds left no trace, so supplier C gets its 5
        repeat(5) { assertTrue(limiter.tryAcquire(USER_C), "call ${it + 1}") }
    }

    @Test
    fun `tryAcquire warns once, with the supplier, when it starts refusing`() {
        // Arrange
        val events = captureLogEvents()
        repeat(5) { limiter.tryAcquire(USER_A) }

        // Act: three refusals in a row
        repeat(3) { limiter.tryAcquire(USER_A) }

        // Assert: one line, so a loop cannot flood the log, and it names who reached the limit
        val warning = events.list.single()
        assertEquals(Level.WARN, warning.level)
        assertTrue(warning.formattedMessage.contains("user $USER_A"), warning.formattedMessage)
    }

    @Test
    fun `tryAcquire tells how many calls it refused once it accepts again`() {
        // Arrange
        val events = captureLogEvents()
        repeat(5) { limiter.tryAcquire(USER_A) }
        repeat(3) { limiter.tryAcquire(USER_A) }

        // Act
        limiter.tryAcquire(USER_B)

        // Assert
        val info = events.list.last()
        assertEquals(Level.INFO, info.level)
        assertTrue(info.formattedMessage.contains("3 refused"), info.formattedMessage)
    }

    /** Collects what the limiter logs during one test; detached afterwards so tests do not see each other's logs. */
    private fun captureLogEvents(): ListAppender<ILoggingEvent> {
        val logger = LoggerFactory.getLogger(OpenFoodFactsRateLimiter::class.java) as Logger
        return ListAppender<ILoggingEvent>().apply { start() }.also {
            logger.addAppender(it)
            attached += it
        }
    }

    /** The appenders this test attached, so [detachAppenders] can remove them. */
    private val attached = mutableListOf<ListAppender<ILoggingEvent>>()

    /** Removes the appenders of this test from the logger after each test. */
    @AfterTest
    fun detachAppenders() {
        val logger = LoggerFactory.getLogger(OpenFoodFactsRateLimiter::class.java) as Logger
        attached.forEach { logger.detachAppender(it) }
    }

    private companion object {
        const val USER_A = 7L
        const val USER_B = 8L
        const val USER_C = 9L
        const val USER_D = 10L
    }
}
