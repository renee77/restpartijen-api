package com.restpartijen.api.product

import com.restpartijen.api.product.model.AmbientProduct
import com.restpartijen.api.product.model.FreshProduct
import com.restpartijen.api.product.model.FrozenProduct
import com.restpartijen.api.product.model.SurplusProduct
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.DomainRuleException
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.SupplierSummary
import com.restpartijen.api.testsupport.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class SurplusProductTest {
    // Every test uses the same fixed moment, so results do not depend on when the test runs.
    private val now = Instant.parse("2026-10-01T00:00:00Z")
    private val clock = FixedClock(now)

    // --- shelfLifeRemaining ---

    @Test
    fun `shelfLifeRemaining gives correct difference between now and bestBeforeAt`() {
        // Arrange
        val product = aProduct(bestBeforeAt = now + 3.days)

        // Act
        val shelfLifeRemaining = product.shelfLifeRemaining(clock)

        // Assert
        assertEquals(3.days, shelfLifeRemaining)
    }

    @Test
    fun `shelfLifeRemaining is negative when bestBeforeAt has passed`() {
        // Arrange
        val product = aProduct(bestBeforeAt = now - 2.days)

        // Act
        val shelfLifeRemaining = product.shelfLifeRemaining(clock)

        // Assert
        assertEquals(-2.days, shelfLifeRemaining)
    }

    // --- isExpired ---

    @Test
    fun `isExpired is false when bestBeforeAt is in the future`() {
        // Arrange
        val product = aProduct(bestBeforeAt = now + 1.days)

        // Act + Assert
        assertFalse(product.isExpired(clock))
    }

    @Test
    fun `isExpired is true exactly at bestBeforeAt`() {
        // Arrange: zero time left counts as expired (PricedProduct)
        val product = aProduct(bestBeforeAt = now)

        // Act + Assert
        assertTrue(product.isExpired(clock))
    }

    @Test
    fun `isExpired is true when bestBeforeAt has passed`() {
        // Arrange
        val product = aProduct(bestBeforeAt = now - 1.days)

        // Act + Assert
        assertTrue(product.isExpired(clock))
    }

    // --- validateForListing: rules for every kind (§9.10) ---

    @Test
    fun `validateForListing accepts a valid product`() {
        // Arrange
        val product = aProduct()

        // Act + Assert: no exception means the product is accepted
        product.validateForListing(clock)
    }

    @Test
    fun `validateForListing rejects a bestBeforeAt in the past`() {
        // Arrange
        val product = aProduct(
            bestBeforeAt = now - 1.days,
            pickupWindow = PickupWindow(from = now - 3.days, until = now - 2.days),
        )

        // Act
        val exception = assertFailsWith<DomainRuleException> { product.validateForListing(clock) }

        // Assert: the field shows which rule was broken
        assertEquals("bestBeforeAt", exception.field)
    }

    @Test
    fun `validateForListing rejects a pickup window that ends after bestBeforeAt`() {
        // Arrange
        val product = aProduct(
            bestBeforeAt = now + 3.days,
            pickupWindow = PickupWindow(from = now, until = now + 4.days),
        )

        // Act
        val exception = assertFailsWith<DomainRuleException> { product.validateForListing(clock) }

        // Assert
        assertEquals("pickupWindow", exception.field)
    }

    @Test
    fun `validateForListing rejects a quantity below 1`() {
        // Arrange
        val product = aProduct(quantity = 0)

        // Act
        val exception = assertFailsWith<DomainRuleException> { product.validateForListing(clock) }

        // Assert
        assertEquals("quantity", exception.field)
    }

    // --- validateForListing: maxShelfLife per kind (§9.10) ---

    @Test
    fun `validateForListing accepts a fresh product one day under maxShelfLife`() {
        // Arrange
        val product = aProduct(bestBeforeAt = now + 13.days)

        // Act + Assert
        product.validateForListing(clock)
    }

    @Test
    fun `validateForListing accepts a fresh product exactly on maxShelfLife`() {
        // Arrange
        val product = aProduct(bestBeforeAt = now + 14.days)

        // Act + Assert: the limit itself is allowed
        product.validateForListing(clock)
    }

    @Test
    fun `validateForListing rejects a fresh product one day over maxShelfLife`() {
        // Arrange
        val product = aProduct(bestBeforeAt = now + 15.days)

        // Act
        val exception = assertFailsWith<DomainRuleException> { product.validateForListing(clock) }

        // Assert
        assertEquals("bestBeforeAt", exception.field)
    }

    @Test
    fun `validateForListing accepts a frozen product exactly on maxShelfLife`() {
        // Arrange
        val product = aProduct(category = ProductCategory.FROZEN, bestBeforeAt = now + 1095.days)

        // Act + Assert
        product.validateForListing(clock)
    }

    @Test
    fun `validateForListing rejects a frozen product one day over maxShelfLife`() {
        // Arrange
        val product = aProduct(category = ProductCategory.FROZEN, bestBeforeAt = now + 1096.days)

        // Act
        val exception = assertFailsWith<DomainRuleException> { product.validateForListing(clock) }

        // Assert
        assertEquals("bestBeforeAt", exception.field)
    }

    @Test
    fun `validateForListing accepts an ambient product exactly on maxShelfLife`() {
        // Arrange
        val product = aProduct(category = ProductCategory.AMBIENT, bestBeforeAt = now + 1825.days)

        // Act + Assert
        product.validateForListing(clock)
    }

    @Test
    fun `validateForListing rejects an ambient product one day over maxShelfLife`() {
        // Arrange
        val product = aProduct(category = ProductCategory.AMBIENT, bestBeforeAt = now + 1826.days)

        // Act
        val exception = assertFailsWith<DomainRuleException> { product.validateForListing(clock) }

        // Assert
        assertEquals("bestBeforeAt", exception.field)
    }

    // Builds a valid product; a test passes only the values it is about.
    // Picks the subclass by category, because a sealed class cannot be instantiated itself.
    private fun aProduct(
        category: ProductCategory = ProductCategory.FRESH,
        id: Long = 1,
        supplier: SupplierSummary = SupplierSummary(id = 1, name = "Bakkerij Test", latitude = 52.0, longitude = 5.0),
        name: String = "Volkoren brood",
        barcode: String? = null,
        originalPrice: Money = Money.fromCents(350),
        bestBeforeAt: Instant = now + 3.days,
        quantity: Int = 1,
        status: ProductStatus = ProductStatus.LISTED,
        pickupWindow: PickupWindow = PickupWindow(from = now, until = now + 2.days),
        allergens: Set<Allergen> = emptySet(),
    ): SurplusProduct = when (category) {
        ProductCategory.FRESH ->
            FreshProduct(id, supplier, name, barcode, originalPrice, bestBeforeAt, quantity, status, pickupWindow, allergens)

        ProductCategory.FROZEN ->
            FrozenProduct(id, supplier, name, barcode, originalPrice, bestBeforeAt, quantity, status, pickupWindow, allergens)

        ProductCategory.AMBIENT ->
            AmbientProduct(id, supplier, name, barcode, originalPrice, bestBeforeAt, quantity, status, pickupWindow, allergens)
    }
}
