package com.restpartijen.api.reservation

import com.restpartijen.api.reservation.model.SearchCriteria
import com.restpartijen.api.reservation.service.ProductSearchService
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.PriceBreakdown
import com.restpartijen.api.shared.PriceProvider
import com.restpartijen.api.shared.PricedProduct
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductView
import com.restpartijen.api.testsupport.FixedClock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/** TC-04: searching and filtering the offer (US-04, §5.4). Only fakes: the search opens no transaction itself. */
class ProductSearchServiceTest {

    private val now = Instant.parse("2026-10-20T12:00:00Z")

    /**
     * FakeProduct counts its shelf life to the end of the pickup window, so [hoursLeft] sets the remaining shelf
     * life.
     */
    private fun product(
        id: Long,
        hoursLeft: Int,
        category: ProductCategory = ProductCategory.FRESH,
        originalCents: Long = 500,
        status: ProductStatus = ProductStatus.LISTED,
    ) = FakeProduct(
        id = id,
        pickupWindow = PickupWindow(now - 1.hours, now + hoursLeft.hours),
        status = status,
        category = category,
        originalPrice = Money.fromCents(originalCents),
    )

    // 60% off by default, so the current price always differs from the original one
    private fun service(vararg products: ProductView, prices: PriceProvider = DiscountPriceProvider(60)) =
        ProductSearchService(FakeProductReader(products.toList()), prices, FixedClock(now))

    @Test
    fun `without filters only listed products come back, soonest best-before first`() = runTest {
        // Arrange
        val service =
            service(
                product(1, hoursLeft = 30),
                product(2, hoursLeft = 5),
                product(3, hoursLeft = 10, status = ProductStatus.RESERVED),
            )
        // Act
        val results = service.search(SearchCriteria())
        // Assert
        assertEquals(listOf(2L, 1L), results.map { it.product.id })
    }

    @Test
    fun `filtering on category returns only that category`() = runTest {
        // Arrange
        val service =
            service(
                product(1, hoursLeft = 5, category = ProductCategory.FRESH),
                product(2, hoursLeft = 5, category = ProductCategory.FROZEN),
            )
        // Act
        val results = service.search(SearchCriteria(category = ProductCategory.FRESH))
        // Assert
        assertEquals(listOf(1L), results.map { it.product.id })
    }

    @Test
    fun `filtering on maxPrice uses the discounted price, not the original one`() = runTest {
        // Arrange: 5.00 becomes 2.00 (fits under 3.00), 10.00 becomes 4.00 (does not)
        val service =
            service(product(1, hoursLeft = 5, originalCents = 500), product(2, hoursLeft = 5, originalCents = 1000))
        // Act
        val results = service.search(SearchCriteria(maxPrice = Money.fromEuros(3.00)))
        // Assert
        assertEquals(listOf(1L), results.map { it.product.id })
    }

    @Test
    fun `filtering on minShelfLife returns only products with at least that much time left`() = runTest {
        // Arrange
        val service = service(product(1, hoursLeft = 5), product(2, hoursLeft = 48))
        // Act
        val results = service.search(SearchCriteria(minShelfLife = 24.hours))
        // Assert
        assertEquals(listOf(2L), results.map { it.product.id })
    }

    @Test
    fun `several filters together work as AND`() = runTest {
        // Arrange: only product 1 passes all three; each other product fails on exactly one filter
        val service = service(
            product(1, hoursLeft = 48),
            product(2, hoursLeft = 48, category = ProductCategory.FROZEN),
            product(3, hoursLeft = 48, originalCents = 1000),
            product(4, hoursLeft = 5),
        )
        // Act
        val results = service.search(SearchCriteria(ProductCategory.FRESH, Money.fromEuros(3.00), 24.hours))
        // Assert
        assertEquals(listOf(1L), results.map { it.product.id })
    }

    @Test
    fun `a product without a price is left out, because it just expired`() = runTest {
        // Arrange: the price provider returns null, as the real one does for an expired product (B-18)
        val service = service(product(1, hoursLeft = 5), prices = FakePriceProvider(null))
        // Act
        val results = service.search(SearchCriteria())
        // Assert
        assertTrue(results.isEmpty())
    }
}

/** Gives every product the same discount, so the current price is lower than the original one. */
private class DiscountPriceProvider(private val percentage: Int) : PriceProvider {
    override fun priceBreakdown(product: PricedProduct): PriceBreakdown {
        val current = Money.fromCents(product.originalPrice.cents * (100 - percentage) / 100)
        return PriceBreakdown(product.originalPrice, percentage, current)
    }

    override fun currentPrice(product: PricedProduct): Money = priceBreakdown(product).currentPrice
}
