package com.restpartijen.api.testsupport

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class FixedClockTest {
    @Test
    fun `now() should return the fixed instant`() {
        // Arrange
        val clock = FixedClock(Instant.parse("2026-09-26T12:00:00Z"))
        // Act
        val now1 = clock.now()
        val now2 = clock.now()
        // Assert
        assertEquals(Instant.parse("2026-09-26T12:00:00Z"), now1)
        assertEquals(Instant.parse("2026-09-26T12:00:00Z"), now2)
    }
}