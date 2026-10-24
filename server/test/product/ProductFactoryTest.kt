package com.restpartijen.api.product

import com.restpartijen.api.product.model.AmbientProduct
import com.restpartijen.api.product.model.FreshProduct
import com.restpartijen.api.product.model.FrozenProduct
import com.restpartijen.api.product.model.SurplusProduct.ProductFactory
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.SupplierSummary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * Unit test for ProductFactory (§9.3): the factory picks the subclass that belongs to the category
 * and passes every value on unchanged.
 *
 * Calls ProductFactory.create directly instead of the aProduct helper, because that helper uses the factory itself.
 */
class ProductFactoryTest {
    private val now = Instant.parse("2026-10-01T00:00:00Z")
    private val supplier = SupplierSummary(id = 7, name = "Bakkerij Test", latitude = 51.59, longitude = 4.78)
    private val pickupWindow = PickupWindow(from = now, until = now + 2.days)

    @Test
    fun `create returns a FreshProduct for category FRESH`() {
        // Act
        val product = createWith(ProductCategory.FRESH)

        // Assert
        assertIs<FreshProduct>(product)
    }

    @Test
    fun `create returns a FrozenProduct for category FROZEN`() {
        // Act
        val product = createWith(ProductCategory.FROZEN)

        // Assert
        assertIs<FrozenProduct>(product)
    }

    @Test
    fun `create returns an AmbientProduct for category AMBIENT`() {
        // Act
        val product = createWith(ProductCategory.AMBIENT)

        // Assert
        assertIs<AmbientProduct>(product)
    }

    @Test
    fun `create passes every value on to the product`() {
        // Act
        val product = createWith(ProductCategory.FROZEN)

        // Assert: compared as one list, so a failure shows every value at once
        assertEquals(
            listOf(
                42L, ProductCategory.FROZEN, supplier, "Diepvries spinazie", "8710400000002", Money.fromCents(499),
                now + 60.days, 3, ProductStatus.RESERVED, pickupWindow, setOf(Allergen.MILK),
            ),
            product.fields(),
        )
    }

    /** Every test uses the same values; only the category differs. */
    private fun createWith(category: ProductCategory) = ProductFactory.create(
        category = category,
        id = 42,
        name = "Diepvries spinazie",
        barcode = "8710400000002",
        originalPrice = Money.fromCents(499),
        supplier = supplier,
        bestBeforeAt = now + 60.days,
        quantity = 3,
        status = ProductStatus.RESERVED,
        pickupWindow = pickupWindow,
        allergens = setOf(Allergen.MILK),
    )
}
