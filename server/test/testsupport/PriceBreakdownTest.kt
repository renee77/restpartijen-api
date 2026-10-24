package com.restpartijen.api.testsupport

import com.restpartijen.api.shared.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PriceBreakdownTest {
    val testPrice = Money.fromCents(349)
    val testCurrentPrice = Money.fromCents(300)

    @Test
    fun `current price above original price is rejected`() {
        // Arrange: a current price (400) that is higher than the original price (349)
        val originalPrice = Money.fromCents(349)
        val currentPrice = Money.fromCents(400)

        // Act + Assert: building the breakdown must throw
        assertFailsWith<IllegalArgumentException> {
            PriceBreakdown(
                originalPrice = originalPrice,
                discountPercentage = 10,
                currentPrice = currentPrice,
            )
        }
    }

    @Test
    fun `discount percentage below 0 is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            PriceBreakdown(
                originalPrice = testPrice,
                discountPercentage = -20,
                currentPrice = testCurrentPrice,
            )
        }
    }

    @Test
    fun `discount percentage above 100 is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            PriceBreakdown(
                originalPrice = testPrice,
                discountPercentage = 120,
                currentPrice = testCurrentPrice,
            )
        }
    }

    @Test
    fun `current price equal to original price is accepted`() {
        // Arrange: a price that is not discounted at all
        val price = Money.fromCents(349)

        // Act: building the breakdown must not throw
        val breakdown = PriceBreakdown(
            originalPrice = price,
            discountPercentage = 0,
            currentPrice = price,
        )

        // Assert: the values are kept as given
        assertEquals(price, breakdown.originalPrice)
        assertEquals(price, breakdown.currentPrice)
        assertEquals(0, breakdown.discountPercentage)
    }

    @Test
    fun `discount percentage of 0 is accepted`() {
        // Arrange and Act
        val breakdown = PriceBreakdown(
            originalPrice = testPrice,
            discountPercentage = 0,
            currentPrice = testCurrentPrice,
        )

        // Assert: The values remain the same
        assertEquals(testPrice, breakdown.originalPrice)
        assertEquals(testCurrentPrice, breakdown.currentPrice)
        assertEquals(0, breakdown.discountPercentage)
    }
}
