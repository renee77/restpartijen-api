package com.restpartijen.api.product

import com.restpartijen.api.product.dto.PickupWindowRequest
import com.restpartijen.api.product.dto.UpdateProductRequest
import com.restpartijen.api.product.dto.toCommand
import com.restpartijen.api.product.dto.validateUpdateProductRequest
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ValidationException
import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.days

/**
 * The request for PUT /api/v1/products/{id} (US-03, A13): turning it into a command, and its shape check.
 * The shape rules are shared with POST; their edge cases (blank, 255 characters) are in
 * PlaceProductRequestValidatorTest. Here one case per rule shows that PUT uses them too.
 */
class UpdateProductRequestTest {

    private fun aRequest(
        name: String = "Volkoren brood",
        barcode: String? = null,
        originalPrice: Double = 3.49,
        pickupWindow: PickupWindowRequest? = PickupWindowRequest(from = testNow, until = testNow + 1.days),
    ) = UpdateProductRequest(
        name = name,
        originalPrice = originalPrice,
        bestBeforeAt = testNow + 3.days,
        quantity = 2,
        barcode = barcode,
        pickupWindow = pickupWindow,
        allergens = setOf(Allergen.MILK),
    )

    private fun fieldOfError(request: UpdateProductRequest): String? =
        assertFailsWith<ValidationException> { validateUpdateProductRequest(request) }.field

    @Test
    fun `toCommand turns the price in euros into whole cents and keeps the other fields`() {
        // Act
        val command = aRequest(name = "Wit brood", barcode = "8710400000001", originalPrice = 2.49).toCommand()

        // Assert
        assertEquals(Money.fromCents(249), command.originalPrice)
        assertEquals("Wit brood", command.name)
        assertEquals("8710400000001", command.barcode)
        assertEquals(testNow + 3.days, command.bestBeforeAt)
        assertEquals(2, command.quantity)
        assertEquals(setOf(Allergen.MILK), command.allergens)
    }

    @Test
    fun `toCommand turns the pickup window into the domain type`() {
        // Act
        val command = aRequest().toCommand()

        // Assert
        assertEquals(PickupWindow(from = testNow, until = testNow + 1.days), command.pickupWindow)
    }

    @Test
    fun `toCommand keeps a missing pickup window missing, for the service to refuse`() {
        // Act
        val command = aRequest(pickupWindow = null).toCommand()

        // Assert: no window is a domain rule (422 in the service), not a shape error
        assertEquals(null, command.pickupWindow)
    }

    @Test
    fun `a valid request passes`() {
        assertEquals(ValidationResult.Valid, validateUpdateProductRequest(aRequest()))
    }

    @Test
    fun `an empty name is refused`() {
        assertEquals("name", fieldOfError(aRequest(name = " ")))
    }

    @Test
    fun `a barcode longer than 255 characters is refused`() {
        assertEquals("barcode", fieldOfError(aRequest(barcode = "1".repeat(256))))
    }

    @Test
    fun `a negative price is refused`() {
        assertEquals("originalPrice", fieldOfError(aRequest(originalPrice = -0.01)))
    }

    @Test
    fun `a pickup window that does not start before it ends is refused`() {
        val window = PickupWindowRequest(from = testNow + 1.days, until = testNow)
        assertEquals("pickupWindow", fieldOfError(aRequest(pickupWindow = window)))
    }
}
