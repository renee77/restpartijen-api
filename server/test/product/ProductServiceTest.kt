package com.restpartijen.api.product

import com.restpartijen.api.product.model.OpeningHours
import com.restpartijen.api.product.service.PlaceProductCommand
import com.restpartijen.api.product.service.UpdateProductCommand
import com.restpartijen.api.shared.DomainRuleException
import com.restpartijen.api.shared.ForbiddenException
import com.restpartijen.api.shared.IllegalStateTransitionException
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
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class ProductServiceTest {
    @Test
    fun `getProduct returns the product when the id exists`() = runTest {
        // Arrange
        val repository = fakeProductRepository(
            aProduct(id = 1, name = "Volkoren brood"),
        )
        val service = productService(repository, fakeSupplierRepository(), FixedPriceProvider(), FixedClock(testNow))
        // Act
        val detail = service.getProduct(1)
        // Assert
        assertEquals("Volkoren brood", detail.product.name)
    }

    @Test
    fun `getProduct throws NotFoundException when the id does not exist`() = runTest {
        // Arrange
        val service = productService(
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
        val service = productService(
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
        val service = productService(
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
        val service = productService(
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
        val service = productService(
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
        val service = productService(
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
        val service = productService(
            repository, fakeSupplierRepository(ownerUserId = 7), FixedPriceProvider(), FixedClock(testNow),
        )

        // Act + Assert
        assertFailsWith<ForbiddenException> { service.createProduct(userId = 8, command = aCommand()) }
        assertEquals(emptyList(), repository.findAll())
    }

    @Test
    fun `createProduct derives the pickup window from the opening hours of the supplier`() = runTest {
        // Arrange: open every day 08:00-21:00; testNow is Thu 1 Oct 02:00 in Amsterdam, best-before Sun 4 Oct 02:00
        val service = productService(
            fakeProductRepository(), fakeSupplierRepository(openingHours = anOpenWeek), FixedPriceProvider(),
            FixedClock(testNow),
        )

        // Act: the window in the command is ignored; it leaves the request in step 8
        val detail = service.createProduct(userId = 7, command = aCommand(bestBeforeAt = testNow + 3.days))

        // Assert: from the first opening (Thu 08:00) to the last closing before best-before (Sat 21:00)
        assertEquals(
            PickupWindow(from = Instant.parse("2026-10-01T06:00:00Z"), until = Instant.parse("2026-10-03T19:00:00Z")),
            detail.product.pickupWindow,
        )
    }

    @Test
    fun `createProduct refuses a supplier that is not open before bestBeforeAt and stores nothing`() = runTest {
        // Arrange: open on Saturday only, and the product expires on Friday (§5.10)
        val repository = fakeProductRepository()
        val saturday = OpeningHours(DayOfWeek.SATURDAY, opensAt = LocalTime(9, 0), closesAt = LocalTime(18, 0))
        val service = productService(
            repository, fakeSupplierRepository(openingHours = listOf(saturday)), FixedPriceProvider(),
            FixedClock(testNow),
        )

        // Act
        val error = assertFailsWith<DomainRuleException> {
            service.createProduct(userId = 7, command = aCommand(bestBeforeAt = testNow + 1.days))
        }

        // Assert
        assertEquals("bestBeforeAt", error.field)
        assertEquals(emptyList(), repository.findAll())
    }

    @Test
    fun `createProduct refuses a best-before moment in the past and stores nothing`() = runTest {
        // Arrange: the calculator reports Expired before it looks at the opening hours
        val repository = fakeProductRepository()
        val service = productService(repository, fakeSupplierRepository(), FixedPriceProvider(), FixedClock(testNow))

        // Act
        val error = assertFailsWith<DomainRuleException> {
            service.createProduct(userId = 7, command = aCommand(bestBeforeAt = testNow - 1.days))
        }

        // Assert: the same field and message as validateForListing, so the answer does not depend on the order
        assertEquals("bestBeforeAt", error.field)
        assertEquals("bestBeforeAt must be in the future", error.message)
        assertEquals(emptyList(), repository.findAll())
    }

    @Test
    fun `createProduct checks the listing rules and stores nothing when one is broken`() = runTest {
        // Arrange: a quantity of 0 breaks validateForListing (422); the window itself is fine
        val repository = fakeProductRepository()
        val service = productService(repository, fakeSupplierRepository(), FixedPriceProvider(), FixedClock(testNow))

        // Act
        val error = assertFailsWith<DomainRuleException> {
            service.createProduct(userId = 7, command = aCommand(quantity = 0))
        }

        // Assert
        assertEquals("quantity", error.field)
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
        val service = productService(
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
        val service = productService(
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
        val service = productService(repository, fakeSupplierRepository(aSupplier), ExpiryPriceProvider(clock), clock)

        // Act
        val details = service.getSupplierProducts(aSupplier.id)

        // Assert
        assertEquals(listOf(1L), details.map { it.product.id })
    }

    @Test
    fun `getSupplierProducts returns an empty list for a supplier without products`() = runTest {
        // Arrange
        val service = productService(
            fakeProductRepository(), fakeSupplierRepository(aSupplier), FixedPriceProvider(), FixedClock(testNow),
        )

        // Act
        val details = service.getSupplierProducts(aSupplier.id)

        // Assert
        assertEquals(emptyList(), details)
    }

    // US-03 delete. The fake supplier repository knows aSupplier, owned by user 7 (ProductFakes).
    @Test
    fun `deleteProduct removes an own LISTED product`() = runTest {
        // Arrange
        val repository = fakeProductRepository(aProduct(id = 1, supplier = aSupplier))
        val service = productService(
            repository, fakeSupplierRepository(aSupplier, ownerUserId = 7), FixedPriceProvider(), FixedClock(testNow),
        )

        // Act
        service.deleteProduct(userId = 7, productId = 1)

        // Assert
        assertNull(repository.findById(1))
    }

    @Test
    fun `deleteProduct throws NotFoundException for an unknown product, before the ownership check`() = runTest {
        // Arrange: user 8 has no supplier, so a 403 here would mean the order of the checks is wrong
        val service = productService(
            fakeProductRepository(), fakeSupplierRepository(aSupplier, ownerUserId = 7), FixedPriceProvider(),
            FixedClock(testNow),
        )

        // Act + Assert
        assertFailsWith<NotFoundException> {
            service.deleteProduct(userId = 8, productId = 999)
        }
    }

    @Test
    fun `deleteProduct refuses a product of another supplier and keeps it`() = runTest {
        // Arrange
        val repository = fakeProductRepository(aProduct(id = 1, supplier = aSupplier.copy(id = 2)))
        val service = productService(
            repository, fakeSupplierRepository(aSupplier, ownerUserId = 7), FixedPriceProvider(), FixedClock(testNow),
        )

        // Act + Assert
        assertFailsWith<ForbiddenException> {
            service.deleteProduct(userId = 7, productId = 1)
        }
        assertNotNull(repository.findById(1))
    }

    // §5.3: only LISTED can be deleted. The repository refuses the rest (false); the service turns that into a 409.
    @Test
    fun `deleteProduct refuses a RESERVED product with IllegalStateTransitionException and keeps it`() = runTest {
        // Arrange
        val repository = fakeProductRepository(aProduct(id = 1, supplier = aSupplier, status = ProductStatus.RESERVED))
        val service = productService(
            repository, fakeSupplierRepository(aSupplier, ownerUserId = 7), FixedPriceProvider(), FixedClock(testNow),
        )

        // Act + Assert
        assertFailsWith<IllegalStateTransitionException> {
            service.deleteProduct(userId = 7, productId = 1)
        }
        assertEquals(ProductStatus.RESERVED, repository.findById(1)?.status)
    }

    // FixedPriceProvider gives every product the same answer; this one gives no price (expired, B-18)
    // only to a product whose shelf life has run out, so one list can hold both kinds.
    private class ExpiryPriceProvider(private val clock: Clock) : PriceProvider {
        override fun currentPrice(product: PricedProduct): Money? = priceBreakdown(product)?.currentPrice
        override fun priceBreakdown(product: PricedProduct): PriceBreakdown? =
            aBreakdown.takeIf { product.shelfLifeRemaining(clock) > Duration.ZERO }
    }

    // US-03 update (A13): only a LISTED product, every field of POST except category.
    @Test
    fun `updateProduct stores the new fields and keeps id, category, supplier and status`() = runTest {
        // Arrange
        val repository = fakeProductRepository(
            aProduct(id = 1, category = ProductCategory.FROZEN, supplier = aSupplier, name = "Diepvries spinazie"),
        )
        val service = productService(
            repository, fakeSupplierRepository(aSupplier, ownerUserId = 7), FixedPriceProvider(aBreakdown),
            FixedClock(testNow),
        )
        val command = anUpdateCommand(name = "Diepvries erwten", originalPrice = Money.fromCents(199), quantity = 5)

        // Act
        val detail = service.updateProduct(userId = 7, productId = 1, command = command)

        // Assert: the answer and the stored product both hold the new values
        val stored = repository.findById(1)!!
        for (product in listOf(detail.product, stored)) {
            assertEquals("Diepvries erwten", product.name)
            assertEquals(Money.fromCents(199), product.originalPrice)
            assertEquals(5, product.quantity)
            assertEquals(1L, product.id)
            assertEquals(ProductCategory.FROZEN, product.category)
            assertEquals(aSupplier, product.supplier)
            assertEquals(ProductStatus.LISTED, product.status)
        }
        assertEquals(aBreakdown, detail.price)
    }

    @Test
    fun `updateProduct throws NotFoundException for an unknown product, before the ownership check`() = runTest {
        // Arrange: user 8 has no supplier, so a 403 here would mean the order of the checks is wrong
        val service = productService(
            fakeProductRepository(), fakeSupplierRepository(aSupplier, ownerUserId = 7), FixedPriceProvider(),
            FixedClock(testNow),
        )

        // Act + Assert
        assertFailsWith<NotFoundException> {
            service.updateProduct(userId = 8, productId = 999, command = anUpdateCommand())
        }
    }

    @Test
    fun `updateProduct refuses a product of another supplier and keeps it unchanged`() = runTest {
        // Arrange
        val repository = fakeProductRepository(aProduct(id = 1, supplier = aSupplier.copy(id = 2), name = "Croissant"))
        val service = productService(
            repository, fakeSupplierRepository(aSupplier, ownerUserId = 7), FixedPriceProvider(), FixedClock(testNow),
        )

        // Act + Assert
        assertFailsWith<ForbiddenException> {
            service.updateProduct(userId = 7, productId = 1, command = anUpdateCommand(name = "Wit brood"))
        }
        assertEquals("Croissant", repository.findById(1)?.name)
    }

    // 409 before 422: a product that may not change at all should not get a message about its dates.
    @Test
    fun `updateProduct refuses a RESERVED product with 409, even when the new dates break a rule`() = runTest {
        // Arrange
        val repository = fakeProductRepository(
            aProduct(id = 1, supplier = aSupplier, status = ProductStatus.RESERVED, name = "Croissant"),
        )
        val service = productService(
            repository, fakeSupplierRepository(aSupplier, ownerUserId = 7), FixedPriceProvider(), FixedClock(testNow),
        )
        val command = anUpdateCommand(
            name = "Wit brood",
            bestBeforeAt = testNow - 1.days,
            pickupWindow = PickupWindow(from = testNow - 2.days, until = testNow - 1.days),
        )

        // Act + Assert
        assertFailsWith<IllegalStateTransitionException> {
            service.updateProduct(userId = 7, productId = 1, command = command)
        }
        assertEquals("Croissant", repository.findById(1)?.name)
    }

    @Test
    fun `updateProduct derives a new pickup window from the opening hours and the moment of updating`() = runTest {
        // Arrange: the stored window (testNow to +2 days) differs from the derived one, so PUT must recalculate it
        val stored = PickupWindow(from = testNow, until = testNow + 2.days)
        val repository = fakeProductRepository(aProduct(id = 1, supplier = aSupplier, pickupWindow = stored))
        val service = productService(
            repository, fakeSupplierRepository(aSupplier, ownerUserId = 7, openingHours = anOpenWeek),
            FixedPriceProvider(), FixedClock(testNow),
        )

        // Act: the window in the command is ignored; it leaves the request in step 8
        val command = anUpdateCommand(bestBeforeAt = testNow + 3.days)
        val detail = service.updateProduct(userId = 7, productId = 1, command = command)

        // Assert: from the first opening (Thu 08:00) to the last closing before best-before (Sat 21:00), also stored
        val derived = PickupWindow(
            from = Instant.parse("2026-10-01T06:00:00Z"),
            until = Instant.parse("2026-10-03T19:00:00Z"),
        )
        assertEquals(derived, detail.product.pickupWindow)
        assertEquals(derived, repository.findById(1)?.pickupWindow)
    }

    @Test
    fun `updateProduct refuses a supplier that is not open before bestBeforeAt and keeps the product`() = runTest {
        // Arrange: open on Saturday only, and the new best-before is Friday (§5.10)
        val repository = fakeProductRepository(aProduct(id = 1, supplier = aSupplier, name = "Croissant"))
        val saturday = OpeningHours(DayOfWeek.SATURDAY, opensAt = LocalTime(9, 0), closesAt = LocalTime(18, 0))
        val service = productService(
            repository, fakeSupplierRepository(aSupplier, ownerUserId = 7, openingHours = listOf(saturday)),
            FixedPriceProvider(), FixedClock(testNow),
        )

        // Act
        val error = assertFailsWith<DomainRuleException> {
            val command = anUpdateCommand(name = "Wit brood", bestBeforeAt = testNow + 1.days)
            service.updateProduct(userId = 7, productId = 1, command = command)
        }

        // Assert
        assertEquals("bestBeforeAt", error.field)
        assertEquals("Croissant", repository.findById(1)?.name)
    }

    @Test
    fun `updateProduct refuses a best-before moment in the past and keeps the product`() = runTest {
        // Arrange: the calculator reports Expired before it looks at the opening hours
        val repository = fakeProductRepository(aProduct(id = 1, supplier = aSupplier, name = "Croissant"))
        val service = productService(
            repository, fakeSupplierRepository(aSupplier, ownerUserId = 7), FixedPriceProvider(), FixedClock(testNow),
        )

        // Act
        val error = assertFailsWith<DomainRuleException> {
            val command = anUpdateCommand(name = "Wit brood", bestBeforeAt = testNow - 1.days)
            service.updateProduct(userId = 7, productId = 1, command = command)
        }

        // Assert: the same field and message as validateForListing
        assertEquals("bestBeforeAt", error.field)
        assertEquals("bestBeforeAt must be in the future", error.message)
        assertEquals("Croissant", repository.findById(1)?.name)
    }

    @Test
    fun `updateProduct checks the listing rules and stores nothing when one is broken`() = runTest {
        // Arrange: a quantity of 0 breaks validateForListing (422); the window itself is fine
        val repository = fakeProductRepository(aProduct(id = 1, supplier = aSupplier, name = "Croissant"))
        val service = productService(
            repository, fakeSupplierRepository(aSupplier, ownerUserId = 7), FixedPriceProvider(), FixedClock(testNow),
        )

        // Act
        val error = assertFailsWith<DomainRuleException> {
            val command = anUpdateCommand(name = "Wit brood", quantity = 0)
            service.updateProduct(userId = 7, productId = 1, command = command)
        }

        // Assert
        assertEquals("quantity", error.field)
        assertEquals("Croissant", repository.findById(1)?.name)
    }

    private fun anUpdateCommand(
        name: String = "Volkoren brood",
        originalPrice: Money = Money.fromCents(349),
        quantity: Int = 2,
        bestBeforeAt: kotlin.time.Instant = testNow + 3.days,
        pickupWindow: PickupWindow? = PickupWindow(from = testNow, until = testNow + 1.days),
    ) = UpdateProductCommand(
        name = name,
        originalPrice = originalPrice,
        bestBeforeAt = bestBeforeAt,
        quantity = quantity,
        pickupWindow = pickupWindow,
    )

    private fun aCommand(
        bestBeforeAt: kotlin.time.Instant = testNow + 3.days,
        quantity: Int = 2,
        pickupWindow: PickupWindow? = PickupWindow(from = testNow, until = testNow + 1.days),
    ) = PlaceProductCommand(
        category = ProductCategory.FRESH,
        name = "Volkoren brood",
        originalPrice = Money.fromCents(349),
        bestBeforeAt = bestBeforeAt,
        quantity = quantity,
        pickupWindow = pickupWindow,
    )
}
