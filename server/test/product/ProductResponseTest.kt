package com.restpartijen.api.product

import com.restpartijen.api.product.dto.PickupWindowResponse
import com.restpartijen.api.product.dto.ProductResponse
import com.restpartijen.api.product.dto.toResponse
import com.restpartijen.api.product.service.ProductDetail
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.PriceBreakdown
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ProductStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

/**
 * The answer of POST /api/v1/products and GET /api/v1/products/{id}: every field of the product (§5.1, TC-01).
 * Amounts in euros (B-25) and times as ISO-8601 text, like ReservationResponse.
 * The price breakdown and the status come from the ProductDetail of the service (§5.7, SD-3).
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
        // 3.49 with 40% off is 2.09: the cent that is left over is dropped (§9.4)
        val price = PriceBreakdown(Money.fromCents(349), discountPercentage = 40, currentPrice = Money.fromCents(209))

        // Act
        val response = ProductDetail(product, price, ProductStatus.LISTED).toResponse()

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
                discountPercentage = 40,
                currentPrice = 2.09,
            ),
            response,
        )
    }

    @Test
    fun `toResponse shows the status of the detail and no price when expired`() {
        // Arrange: the product is still LISTED in storage, the service decided it shows as EXPIRED
        val detail = ProductDetail(aProduct(status = ProductStatus.LISTED), price = null, status = ProductStatus.EXPIRED)

        // Act
        val response = detail.toResponse()

        // Assert
        assertEquals("EXPIRED", response.status)
        assertNull(response.discountPercentage)
        assertNull(response.currentPrice)
    }
}
