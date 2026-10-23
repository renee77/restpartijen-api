package com.restpartijen.api.product

import com.restpartijen.api.product.dto.toBestBeforeAt
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * A best-before date counts up to the start of the next day in Amsterdam (decision 23 Oct 2026).
 * The expected values are worked out by hand: summer time is UTC+2, winter time UTC+1, and the clocks go back
 * on Sunday 25 October 2026.
 */
class BestBeforeDateTest {

    @Test
    fun `a date in summer time ends at 22-00 UTC`() {
        // Act: the next day starts on summer time, so 00:00 is 22:00 UTC
        val bestBeforeAt = LocalDate(2026, 10, 24).toBestBeforeAt()

        // Assert
        assertEquals(Instant.parse("2026-10-24T22:00:00Z"), bestBeforeAt) // Sun 25 Oct 00:00
    }

    @Test
    fun `the day the clocks go back ends on winter time`() {
        // Act: 25 October starts on summer time, but the next day starts on winter time
        val bestBeforeAt = LocalDate(2026, 10, 25).toBestBeforeAt()

        // Assert
        assertEquals(Instant.parse("2026-10-25T23:00:00Z"), bestBeforeAt) // Mon 26 Oct 00:00
    }

    @Test
    fun `a date in winter time ends at 23-00 UTC`() {
        // Act: the example of a label with 13-12-2026
        val bestBeforeAt = LocalDate(2026, 12, 13).toBestBeforeAt()

        // Assert
        assertEquals(Instant.parse("2026-12-13T23:00:00Z"), bestBeforeAt) // Mon 14 Dec 00:00
    }
}
