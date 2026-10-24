package com.restpartijen.api.product

import com.restpartijen.api.product.dto.PlaceProductRequest
import com.restpartijen.api.product.dto.toCommand
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.ProductCategory
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * The request for POST /api/v1/products: reading JSON, and turning it into a command.
 * The shape checks are in PlaceProductRequestValidatorTest.
 */
class PlaceProductRequestTest {

    private fun aRequest(originalPrice: Double = 3.49) = PlaceProductRequest(
        category = ProductCategory.FRESH,
        name = "Volkoren brood",
        originalPrice = originalPrice,
        bestBeforeAt = LocalDate(2026, 10, 4),
        quantity = 2,
    )

    @Test
    fun `JSON with an ISO date and allergen names is read into a request`() {
        // Arrange
        val json = """
            {"category": "FRESH", "name": "Volkoren brood", "originalPrice": 3.49,
             "bestBeforeAt": "2026-10-04", "quantity": 2, "allergens": ["MILK"]}
        """

        // Act
        val request = Json.decodeFromString<PlaceProductRequest>(json)

        // Assert
        assertEquals(LocalDate(2026, 10, 4), request.bestBeforeAt)
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
    fun `toCommand turns the best-before date into the start of the next day in Amsterdam`() {
        // Act
        val command = aRequest().toCommand()

        // Assert: 5 October 00:00 summer time; the cases around the clock change are in BestBeforeDateTest
        assertEquals(Instant.parse("2026-10-04T22:00:00Z"), command.bestBeforeAt)
    }
}
