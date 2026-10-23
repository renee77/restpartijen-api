package com.restpartijen.api.product

import com.restpartijen.api.product.dto.PlaceProductRequest
import com.restpartijen.api.product.dto.validatePlaceProductRequest
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ValidationException
import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.days

/**
 * The shape check of POST /api/v1/products, registered in RequestValidation (§10.2, ADR-05, issue #46).
 * A shape error throws a ValidationException with the field, so StatusPages answers 400 instead of 500.
 * Limits: the name fits the varchar(255) column and a blank text counts as empty;
 * a barcode is 7 to 14 digits, the same rule as the lookup (US-02).
 */
class PlaceProductRequestValidatorTest {

    private fun aRequest(
        name: String = "Volkoren brood",
        barcode: String? = null,
        originalPrice: Double = 3.49,
        bestBeforeAt: LocalDate = LocalDate(2026, 10, 4),
    ) = PlaceProductRequest(
        category = ProductCategory.FRESH,
        name = name,
        originalPrice = originalPrice,
        bestBeforeAt = bestBeforeAt,
        quantity = 2,
        barcode = barcode,
    )

    // Runs the validator and returns the field it names; fails the test when nothing is thrown.
    private fun fieldOfError(request: PlaceProductRequest): String? =
        assertFailsWith<ValidationException> { validatePlaceProductRequest(request) }.field

    @Test
    fun `a valid request passes`() {
        // Act
        val result = validatePlaceProductRequest(aRequest())

        // Assert
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun `a negative price is refused and names the field`() {
        assertEquals("originalPrice", fieldOfError(aRequest(originalPrice = -1.0)))
    }

    @Test
    fun `an empty name is refused`() {
        assertEquals("name", fieldOfError(aRequest(name = "")))
    }

    @Test
    fun `a name of only spaces is refused`() {
        assertEquals("name", fieldOfError(aRequest(name = "   ")))
    }

    @Test
    fun `a name longer than 255 characters is refused`() {
        assertEquals("name", fieldOfError(aRequest(name = "a".repeat(256))))
    }

    @Test
    fun `a name of exactly 255 characters passes`() {
        assertEquals(ValidationResult.Valid, validatePlaceProductRequest(aRequest(name = "a".repeat(255))))
    }

    @Test
    fun `a request without a barcode passes`() {
        assertEquals(ValidationResult.Valid, validatePlaceProductRequest(aRequest(barcode = null)))
    }

    @Test
    fun `an empty barcode is refused`() {
        assertEquals("barcode", fieldOfError(aRequest(barcode = "")))
    }

    @Test
    fun `a barcode of only spaces is refused`() {
        assertEquals("barcode", fieldOfError(aRequest(barcode = "   ")))
    }

    @Test
    fun `a barcode with letters is refused`() {
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
    fun `barcodes of 7, 13 and 14 digits pass`() {
        // UPC-E, EAN-13 and GTIN-14: the same rule as the lookup, so a stored barcode can always be looked up
        for (barcode in listOf("1234567", "8710400000001", "1".repeat(14))) {
            assertEquals(ValidationResult.Valid, validatePlaceProductRequest(aRequest(barcode = barcode)), barcode)
        }
    }

    @Test
    fun `a best-before date after the year 9999 is refused and names the field`() {
        // Arrange: a valid ISO date that the conversion to an instant cannot handle at the edge (500 before)
        val request = aRequest(bestBeforeAt = LocalDate.parse("+999999999-12-31"))

        // Act + Assert
        assertEquals("bestBeforeAt", fieldOfError(request))
    }

    @Test
    fun `a best-before date in the year 9999 passes`() {
        // Act: the last day of the limit itself is allowed; the shelf-life rule then gives a 422 later
        val result = validatePlaceProductRequest(aRequest(bestBeforeAt = LocalDate(9999, 12, 31)))

        // Assert
        assertEquals(ValidationResult.Valid, result)
    }
}
