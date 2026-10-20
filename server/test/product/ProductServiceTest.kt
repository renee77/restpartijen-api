package com.restpartijen.api.product

import com.restpartijen.api.product.service.PlaceProductCommand
import com.restpartijen.api.product.service.ProductService
import com.restpartijen.api.shared.DomainRuleException
import com.restpartijen.api.shared.ForbiddenException
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.NotFoundException
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.testsupport.FixedClock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.days

class ProductServiceTest {
    @Test
    fun `getProduct returns the product when the id exists`() = runTest {
        // Arrange
        val repository = fakeProductRepository(
            aProduct(id = 1, name = "Volkoren brood"),
        )
        val service = ProductService(repository, fakeSupplierRepository(), FixedClock(testNow))
        // Act
        val product = service.getProduct(1)
        // Assert
        assertEquals("Volkoren brood", product.name)
    }

    @Test
    fun `getProduct throws NotFoundException when the id does not exist`() = runTest {
        // Arrange
        val service = ProductService(fakeProductRepository(), fakeSupplierRepository(), FixedClock(testNow))

        // Act + Assert
        assertFailsWith<NotFoundException> {
            service.getProduct(999)
        }
    }

    @Test
    fun `createProduct stores a listed product of the supplier of the user`() = runTest {
        // Arrange
        val repository = fakeProductRepository()
        val service = ProductService(repository, fakeSupplierRepository(aSupplier, ownerUserId = 7), FixedClock(testNow))

        // Act
        val product = service.createProduct(userId = 7, command = aCommand())

        // Assert: stored, with the supplier from the token and status LISTED (TC-01)
        assertEquals(aSupplier, product.supplier)
        assertEquals(ProductStatus.LISTED, product.status)
        assertEquals(listOf(product.id), repository.findAll().map { it.id })
    }

    @Test
    fun `createProduct refuses a user without a supplier`() = runTest {
        // Arrange: user 8 has a SUPPLIER token but no supplier row
        val repository = fakeProductRepository()
        val service = ProductService(repository, fakeSupplierRepository(ownerUserId = 7), FixedClock(testNow))

        // Act + Assert
        assertFailsWith<ForbiddenException> { service.createProduct(userId = 8, command = aCommand()) }
        assertEquals(emptyList(), repository.findAll())
    }

    @Test
    fun `createProduct refuses a product without a pickup window`() = runTest {
        // Arrange: there are no opening hours yet (US-10), so the window cannot be derived
        val service = ProductService(fakeProductRepository(), fakeSupplierRepository(), FixedClock(testNow))

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
        val service = ProductService(repository, fakeSupplierRepository(), FixedClock(testNow))
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
