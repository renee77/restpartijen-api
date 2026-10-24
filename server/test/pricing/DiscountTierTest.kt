package com.restpartijen.api.pricing

import com.restpartijen.api.pricing.model.DiscountTier
import com.restpartijen.api.pricing.model.tierFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class DiscountTierTest {
    val tiers = listOf(
        DiscountTier(48.hours, 10),
        DiscountTier(24.hours, 40),
        DiscountTier(12.hours, 60),
    )

    @Test
    fun `tierfor returns null at 60 hours`() {
        // Act
        val result = tiers.tierFor(60.hours)

        // Assert
        assertNull(result?.percentage)
    }

    @Test
    fun `tierFor returns null at 48 hours and 1 second`() {
        // Act
        val result = tiers.tierFor(48.hours + 1.seconds)

        // Assert
        assertNull(result?.percentage)
    }

    @Test
    fun `tierFor returns 10 percent at 48 hours`() {
        // Act
        val result = tiers.tierFor(48.hours)

        // Assert
        assertEquals(10, result?.percentage)
    }

    @Test
    fun `tierFor returns 40 percent at 24 hours`() {
        // Act
        val result = tiers.tierFor(24.hours)

        // Assert
        assertEquals(40, result?.percentage)
    }

    @Test
    fun `tierFor returns 60 percent at 12 hours`() {
        // Act
        val result = tiers.tierFor(12.hours)

        // Assert
        assertEquals(60, result?.percentage)
    }

    @Test
    fun `tierFor returns 60 percent at 0 hours`() {
        // Act
        val result = tiers.tierFor(0.seconds)

        // Assert
        assertEquals(60, result?.percentage)
    }
}
