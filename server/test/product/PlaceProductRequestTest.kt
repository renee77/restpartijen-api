package com.restpartijen.api.product

import com.restpartijen.api.product.dto.PickupWindowRequest
import com.restpartijen.api.product.dto.PlaceProductRequest
import com.restpartijen.api.product.dto.toCommand
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductCategory
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * The request for POST /api/v1/products: reading JSON, and turning it into a command.
 * The shape checks are in PlaceProductRequestValidatorTest.
 */
class PlaceProductRequestTest {

    private fun aRequest(
        originalPrice: Double = 3.49,
        pickupWindow: PickupWindowRequest? = PickupWindowRequest(from = testNow, until = testNow + 1.days),
    ) = PlaceProductRequest(
        category = ProductCategory.FRESH,
        name = "Volkoren brood",
        originalPrice = originalPrice,
        bestBeforeAt = testNow + 3.days,
        quantity = 2,
        pickupWindow = pickupWindow,
    )

    @Test
    fun `JSON with ISO times and allergen names is read into a request`() {
        // Arrange
        val json = """
            {"category": "FRESH", "name": "Volkoren brood", "originalPrice": 3.49,
             "bestBeforeAt": "2026-10-04T00:00:00Z", "quantity": 2, "allergens": ["MILK"]}
        """

        // Act
        val request = Json.decodeFromString<PlaceProductRequest>(json)

        // Assert
        assertEquals(Instant.parse("2026-10-04T00:00:00Z"), request.bestBeforeAt)
        assertEquals(setOf(Allergen.MILK), request.allergens)
    }

    @Test
    fun `toCommand turns the price in euros into whole cents`() {
        // Act
        val command = aRequest(originalPrice = 3.49).toCommand()

        // Assert: B-25, the shared conversion rounds to the nearest cent
        assertEquals(Money.fromCents(349), command.originalPrice)
    }

    @Test
    fun `toCommand turns the pickup window into the domain type`() {
        // Act
        val command = aRequest().toCommand()

        // Assert
        assertEquals(PickupWindow(from = testNow, until = testNow + 1.days), command.pickupWindow)
    }
}
