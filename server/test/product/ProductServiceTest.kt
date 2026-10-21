package com.restpartijen.api.product

import com.restpartijen.api.product.service.PlaceProductCommand
import com.restpartijen.api.product.service.ProductService
import com.restpartijen.api.shared.DomainRuleException
import com.restpartijen.api.shared.ForbiddenException
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.NotFoundException
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.PriceBreakdown
import com.restpartijen.api.shared.PriceProvider
import com.restpartijen.api.shared.PricedProduct
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.testsupport.FixedClock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class ProductServiceTest {
    @Test
    fun `getProduct returns the product when the id exists`() = runTest {
        // Arrange
        val repository = fakeProductRepository(
            aProduct(id = 1, name = "Volkoren brood"),
        )
        val service = ProductService(repository, fakeSupplierRepository(), FixedPriceProvider(), FixedClock(testNow))
        // Act
        val detail = service.getProduct(1)
        // Assert
        assertEquals("Volkoren brood", detail.product.name)
    }

    @Test
    fun `getProduct throws NotFoundException when the id does not exist`() = runTest {
        // Arrange
        val service = ProductService(
            fakeProductRepository(), fakeSupplierRepository(), FixedPriceProvider(), FixedClock(testNow),
        )

        // Act + Assert
        assertFailsWith<NotFoundException> {
            service.getProduct(999)
        }
    }

    @Test
    fun `getProduct includes the price breakdown from the PriceProvider`() = runTest {
        // Arrange: SD-3, F1 asks F3 for the price instead of knowing the discount rules
        val repository = fakeProductRepository(aProduct(id = 1))
        val service = ProductService(
            repository, fakeSupplierRepository(), FixedPriceProvider(aBreakdown), FixedClock(testNow),
        )

        // Act
        val detail = service.getProduct(1)

        // Assert
        assertEquals(aBreakdown, detail.price)
        assertEquals(ProductStatus.LISTED, detail.status)
    }

    @Test
    fun `getProduct shows an expired LISTED product as EXPIRED without a price`() = runTest {
        // Arrange: no price means expired (B-18); the status updater may not have run yet (SD-3)
        val repository = fakeProductRepository(aProduct(id = 1, status = ProductStatus.LISTED))
        val service = ProductService(
            repository, fakeSupplierRepository(), FixedPriceProvider(breakdown = null), FixedClock(testNow),
        )

        // Act
        val detail = service.getProduct(1)

        // Assert
        assertNull(detail.price)
        assertEquals(ProductStatus.EXPIRED, detail.status)
    }

    @Test
    fun `getProduct keeps the status of an expired product that is not LISTED`() = runTest {
        // Arrange: only a LISTED product turns EXPIRED (§9.5); a reservation is handled by F2
        val repository = fakeProductRepository(aProduct(id = 1, status = ProductStatus.RESERVED))
        val service = ProductService(
            repository, fakeSupplierRepository(), FixedPriceProvider(breakdown = null), FixedClock(testNow),
        )

        // Act
        val detail = service.getProduct(1)

        // Assert
        assertNull(detail.price)
        assertEquals(ProductStatus.RESERVED, detail.status)
    }

    @Test
    fun `createProduct stores a listed product of the supplier of the user`() = runTest {
        // Arrange
        val repository = fakeProductRepository()
        val service = ProductService(
            repository, fakeSupplierRepository(aSupplier, ownerUserId = 7), FixedPriceProvider(), FixedClock(testNow),
        )

        // Act
        val detail = service.createProduct(userId = 7, command = aCommand())

        // Assert: stored, with the supplier from the token and status LISTED (TC-01)
        assertEquals(aSupplier, detail.product.supplier)
        assertEquals(ProductStatus.LISTED, detail.status)
        assertEquals(listOf(detail.product.id), repository.findAll().map { it.id })
    }

    @Test
    fun `createProduct includes the price breakdown, like every product answer`() = runTest {
        // Arrange: §10.1, the price belongs to the product and is in every answer with a product
        val service = ProductService(
            fakeProductRepository(), fakeSupplierRepository(), FixedPriceProvider(aBreakdown), FixedClock(testNow),
        )

        // Act
        val detail = service.createProduct(userId = 7, command = aCommand())

        // Assert
        assertEquals(aBreakdown, detail.price)
    }

    @Test
    fun `createProduct refuses a user without a supplier`() = runTest {
        // Arrange: user 8 has a SUPPLIER token but no supplier row
        val repository = fakeProductRepository()
        val service = ProductService(
            repository, fakeSupplierRepository(ownerUserId = 7), FixedPriceProvider(), FixedClock(testNow),
        )

        // Act + Assert
        assertFailsWith<ForbiddenException> { service.createProduct(userId = 8, command = aCommand()) }
        assertEquals(emptyList(), repository.findAll())
    }

    @Test
    fun `createProduct refuses a product without a pickup window`() = runTest {
        // Arrange: there are no opening hours yet (US-10), so the window cannot be derived
        val service = ProductService(
            fakeProductRepository(), fakeSupplierRepository(), FixedPriceProvider(), FixedClock(testNow),
        )

        // Act
        val error = assertFailsWith<DomainRuleException> {
            service.createProduct(userId = 7, command = aCommand(pickupWindow = null))
        }

        // Assert
        assertEquals("pickupWindow", error.field)
    }

    @Test
    fun `createProduct checks the listing rules and stores nothing when one is broken`() = runTest {
        // Arrange: a best-before moment in the past breaks validateForListing (422)
        val repository = fakeProductRepository()
        val service = ProductService(repository, fakeSupplierRepository(), FixedPriceProvider(), FixedClock(testNow))
        val command = aCommand(
            bestBeforeAt = testNow - 1.days,
            pickupWindow = PickupWindow(from = testNow - 2.days, until = testNow - 1.days),
        )

        // Act
        val error = assertFailsWith<DomainRuleException> { service.createProduct(userId = 7, command = command) }

        // Assert
        assertEquals("bestBeforeAt", error.field)
        assertEquals(emptyList(), repository.findAll())
    }

    @Test
    fun `getSupplierProducts returns the current offer of the supplier with its price breakdown`() = runTest {
        // Arrange: the fake repository applies the same filter as the real query (A4)
        val otherSupplier = aSupplier.copy(id = 2)
        val repository = fakeProductRepository(
            aProduct(id = 1, supplier = aSupplier),
            aProduct(id = 2, supplier = aSupplier, status = ProductStatus.RESERVED),
            aProduct(id = 3, supplier = otherSupplier),
        )
        val service = ProductService(
            repository, fakeSupplierRepository(aSupplier), FixedPriceProvider(aBreakdown), FixedClock(testNow),
        )

        // Act
        val details = service.getSupplierProducts(aSupplier.id)

        // Assert
        assertEquals(listOf(1L, 2L), details.map { it.product.id })
        assertEquals(listOf(ProductStatus.LISTED, ProductStatus.RESERVED), details.map { it.status })
        assertEquals(listOf(aBreakdown, aBreakdown), details.map { it.price })
    }

    @Test
    fun `getSupplierProducts throws NotFoundException for an unknown supplier`() = runTest {
        // Arrange
        val service = ProductService(
            fakeProductRepository(), fakeSupplierRepository(aSupplier), FixedPriceProvider(), FixedClock(testNow),
        )

        // Act + Assert
        assertFailsWith<NotFoundException> {
            service.getSupplierProducts(999)
        }
    }

    // A LISTED product past its best-before shows as EXPIRED (SD-3) until the status updater runs (US-08).
    // "Mijn aanbod" shows only LISTED and RESERVED (A4), so the service leaves it out.
    @Test
    fun `getSupplierProducts leaves out a LISTED product that has just expired`() = runTest {
        // Arrange
        val clock = FixedClock(testNow)
        val repository = fakeProductRepository(
            aProduct(id = 1, bestBeforeAt = testNow + 3.days),
            aProduct(id = 2, bestBeforeAt = testNow - 1.days),
        )
        val service = ProductService(repository, fakeSupplierRepository(aSupplier), ExpiryPriceProvider(clock), clock)

        // Act
        val details = service.getSupplierProducts(aSupplier.id)

        // Assert
        assertEquals(listOf(1L), details.map { it.product.id })
    }

    @Test
    fun `getSupplierProducts returns an empty list for a supplier without products`() = runTest {
        // Arrange
        val service = ProductService(
            fakeProductRepository(), fakeSupplierRepository(aSupplier), FixedPriceProvider(), FixedClock(testNow),
        )

        // Act
        val details = service.getSupplierProducts(aSupplier.id)

        // Assert
        assertEquals(emptyList(), details)
    }

    // FixedPriceProvider gives every product the same answer; this one gives no price (expired, B-18)
    // only to a product whose shelf life has run out, so one list can hold both kinds.
    private class ExpiryPriceProvider(private val clock: Clock) : PriceProvider {
        override fun currentPrice(product: PricedProduct): Money? = priceBreakdown(product)?.currentPrice
        override fun priceBreakdown(product: PricedProduct): PriceBreakdown? =
            aBreakdown.takeIf { product.shelfLifeRemaining(clock) > Duration.ZERO }
    }

    private fun aCommand(
        bestBeforeAt: kotlin.time.Instant = testNow + 3.days,
        pickupWindow: PickupWindow? = PickupWindow(from = testNow, until = testNow + 1.days),
    ) = PlaceProductCommand(
        category = ProductCategory.FRESH,
        name = "Volkoren brood",
        originalPrice = Money.fromCents(349),
        bestBeforeAt = bestBeforeAt,
        quantity = 2,
        pickupWindow = pickupWindow,
    )
}
