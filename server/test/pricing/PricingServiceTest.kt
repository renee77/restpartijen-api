package com.restpartijen.api.pricing.service

import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PriceBreakdown
import com.restpartijen.api.shared.PricedProduct
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.testsupport.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class PricingServiceTest {

    private val clock = FixedClock(Instant.fromEpochSeconds(0))
    private val service = PricingService(clock)

    // A fake product with a fixed price, category and remaining shelf life
    private fun fakeProduct(priceCents: Long, productCategory: ProductCategory, remaining: Duration) =
        object : PricedProduct {
            override val originalPrice = Money.fromCents(priceCents)
            override val category = productCategory
            override fun shelfLifeRemaining(clock: Clock): Duration = remaining
        }

    @Test
    fun `349 cents with 60 percent discount gives 139 cents`() {
        // Arrange
        val product = fakeProduct(349, ProductCategory.FRESH, 6.hours)

        // Act
        val price = service.currentPrice(product)

        // Assert
        assertEquals(Money.fromCents(139), price)
    }

    @Test
    fun `price breakdown shows original price, percentage and current price`() {
        // Arrange
        val product = fakeProduct(349, ProductCategory.FRESH, 6.hours)

        // Act
        val breakdown = service.priceBreakdown(product)

        // Assert
        val expected = PriceBreakdown(
            originalPrice = Money.fromCents(349),
            discountPercentage = 60,
            currentPrice = Money.fromCents(139),
        )
        assertEquals(expected, breakdown)
    }
}
