package com.restpartijen.api.product

import com.restpartijen.api.product.dto.UpdateProductRequest
import com.restpartijen.api.product.dto.toCommand
import com.restpartijen.api.product.dto.validateUpdateProductRequest
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.ValidationException
import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

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
    ) = UpdateProductRequest(
        name = name,
        originalPrice = originalPrice,
        bestBeforeAt = LocalDate(2026, 10, 4),
        quantity = 2,
        barcode = barcode,
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
        assertEquals(Instant.parse("2026-10-04T22:00:00Z"), command.bestBeforeAt) // 5 Oct 00:00 in Amsterdam
        assertEquals(2, command.quantity)
        assertEquals(setOf(Allergen.MILK), command.allergens)
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
    fun `a barcode with letters is refused`() {
        // PUT shares the barcode rule with POST and the lookup: 7 to 14 digits, or no barcode
        assertEquals("barcode", fieldOfError(aRequest(barcode = "871040000000A")))
    }

    @Test
    fun `a barcode of 6 digits is refused`() {
        assertEquals("barcode", fieldOfError(aRequest(barcode = "123456")))
    }

    @Test
    fun `a barcode of 15 digits is refused`() {
        assertEquals("barcode", fieldOfError(aRequest(barcode = "1".repeat(15))))
    }

    @Test
    fun `a barcode of 13 digits passes`() {
        assertEquals(ValidationResult.Valid, validateUpdateProductRequest(aRequest(barcode = "8710400000001")))
    }

    @Test
    fun `a negative price is refused`() {
        assertEquals("originalPrice", fieldOfError(aRequest(originalPrice = -0.01)))
    }
}
