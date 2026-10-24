package com.restpartijen.api.product

import com.restpartijen.api.product.service.PlaceProductCommand
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.ProductCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.days

/** The optional parts of placing a product have defaults, so a caller passes only what it has (§11.1). */
class PlaceProductCommandTest {

    @Test
    fun `a command with only the required values has no barcode and no allergens`() {
        // Act
        val command = PlaceProductCommand(
            category = ProductCategory.FRESH,
            name = "Volkoren brood",
            originalPrice = Money.fromCents(349),
            bestBeforeAt = testNow + 3.days,
            quantity = 2,
        )

        // Assert
        assertNull(command.barcode)
        assertEquals(emptySet(), command.allergens)
    }
}
