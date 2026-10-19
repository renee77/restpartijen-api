package com.restpartijen.api.product

import com.restpartijen.api.product.dto.PickupWindowResponse
import com.restpartijen.api.product.dto.ProductResponse
import com.restpartijen.api.product.dto.toResponse
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * The answer of POST /api/v1/products and GET /api/v1/products/{id}: every field of the product (§5.1, TC-01).
 * Amounts in euros (B-25) and times as ISO-8601 text, like ReservationResponse.
 */
class ProductResponseTest {

    @Test
    fun `toResponse gives every field of the product, with the price in euros and times as ISO text`() {
        // Arrange
        val product = aProduct(
            category = ProductCategory.FROZEN,
            id = 12,
            supplier = aSupplier,
            name = "Spinazie",
            barcode = "8710400000001",
            originalPrice = Money.fromCents(349),
            bestBeforeAt = Instant.parse("2026-10-04T00:00:00Z"),
            quantity = 3,
            pickupWindow = PickupWindow(
                from = Instant.parse("2026-10-01T09:00:00Z"),
                until = Instant.parse("2026-10-01T17:00:00Z"),
            ),
            allergens = setOf(Allergen.MILK),
        )

        // Act
        val response = product.toResponse()

        // Assert
        assertEquals(
            ProductResponse(
                id = 12,
                supplierId = aSupplier.id,
                category = "FROZEN",
                name = "Spinazie",
                barcode = "8710400000001",
                originalPrice = 3.49,
                bestBeforeAt = "2026-10-04T00:00:00Z",
                quantity = 3,
                status = "LISTED",
                pickupWindow = PickupWindowResponse(from = "2026-10-01T09:00:00Z", until = "2026-10-01T17:00:00Z"),
                allergens = setOf(Allergen.MILK),
            ),
            response,
        )
    }
}
