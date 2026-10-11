package com.restpartijen.api.pricing

import com.restpartijen.api.pricing.service.PricingService
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.testsupport.FixedClock
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class DiscountPriceTests {
    val fakeClock = FixedClock(Instant.parse("2026-10-11T12:00:00Z"))
    val service = PricingService(fakeClock)


    // Price and corner cases
    @Test
    fun `fresh at exactly 48 hours gives 10 percent`() {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.FRESH,
            remaining = 48.hours
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertEquals(10, result?.discountPercentage)
    }

    @Test
    fun `fresh at 48 hours and one second gives 0` () {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.FRESH,
            remaining = 48.hours + 1.seconds
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertEquals(0, result?.discountPercentage)
    }

    @Test
    fun `fresh at exactly 24 hours gives 40 percent`() {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.FRESH,
            remaining = 24.hours
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertEquals(40, result?.discountPercentage)
    }

    @Test
    fun `fresh at 24 hours and one second gives 10` () {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.FRESH,
            remaining = 24.hours + 1.seconds
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertEquals(10, result?.discountPercentage)
    }

    @Test
    fun `fresh at exactly 12 hours gives 60 percent`() {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.FRESH,
            remaining = 12.hours
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertEquals(60, result?.discountPercentage)
    }

    @Test
    fun `fresh at 12 hours and one second gives 40` () {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.FRESH,
            remaining = 12.hours + 1.seconds
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertEquals(40, result?.discountPercentage)
    }

    @Test
    fun `frozen at exactly 7 days gives 15 percent`() {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.FROZEN,
            remaining = 7.days
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertEquals(15, result?.discountPercentage)
    }

    @Test
    fun `frozen at 7 days and one second gives 0 percent`() {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.FROZEN,
            remaining = 7.days + 1.seconds
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertEquals(0, result?.discountPercentage)
    }

    @Test
    fun `frozen at exactly 24 hours gives 40 percent`() {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.FROZEN,
            remaining = 24.hours
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertEquals(40, result?.discountPercentage)
    }

    @Test
    fun `frozen at 24 hours and one second gives 15 percent`() {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.FROZEN,
            remaining = 24.hours + 1.seconds
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertEquals(15, result?.discountPercentage)
    }

    @Test
    fun `ambient at exactly 14 days gives 15 percent`() {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.AMBIENT,
            remaining = 14.days
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertEquals(15, result?.discountPercentage)
    }

    @Test
    fun `ambient at 14 days and one second gives 0 percent`() {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.AMBIENT,
            remaining = 14.days + 1.seconds
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertEquals(0, result?.discountPercentage)
    }

    @Test
    fun `ambient at exactly 7 days gives 40 percent`() {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.AMBIENT,
            remaining = 7.days
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertEquals(40, result?.discountPercentage)
    }

    @Test
    fun `ambient at 7 days and one second gives 15 percent`() {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.AMBIENT,
            remaining = 7.days + 1.seconds
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertEquals(15, result?.discountPercentage)
    }


    @Test
    fun `current price is never negative`() {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.FRESH,
            remaining = 12.hours
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertNotNull(result)
        assertTrue(result.currentPrice.cents >= 0)
    }

    @Test
    fun `current price never exceeds original price`() {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.FRESH,
            remaining = 48.hours
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertNotNull(result)
        assertTrue(
            result.currentPrice.cents <= result.originalPrice.cents
        )
    }

    @Test
    fun `expired product returns null`() {
        // Arrange
        val product = FakePricedProduct(
            originalPrice = Money.fromCents(1000),
            category = ProductCategory.FRESH,
            remaining = 0.seconds
        )

        // Act
        val result = service.priceBreakdown(product)

        // Assert
        assertNull(result)
    }
}