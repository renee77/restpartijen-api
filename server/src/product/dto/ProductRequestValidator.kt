package com.restpartijen.api.product.dto

import com.restpartijen.api.shared.ValidationException
import io.ktor.server.plugins.requestvalidation.ValidationResult

// Matches the varchar(255) columns in ProductsTable; a longer text would fail on INSERT with a 500.
// Internal, because the barcode lookup only suggests a name that POST and PUT would accept (US-02).
internal const val MAX_TEXT_LENGTH = 255

/**
 * Checks the shape of a POST /api/v1/products body before the route uses it (§10.2, ADR-05).
 *
 * Registered in configureRequestValidation(). Throws ValidationException with the field, so StatusPages
 * answers 400 with `field` filled in (option B). Domain rules, such as a date in the past or a quantity
 * below 1, are not checked here: they belong to the product (validateForListing, 422).
 */
fun validatePlaceProductRequest(request: PlaceProductRequest): ValidationResult =
    validateProductFields(request.name, request.barcode, request.originalPrice, request.pickupWindow)

/**
 * Checks the shape of a PUT /api/v1/products/{id} body (US-03), with the same rules as POST:
 * both bodies carry the same product fields, apart from category.
 */
fun validateUpdateProductRequest(request: UpdateProductRequest): ValidationResult =
    validateProductFields(request.name, request.barcode, request.originalPrice, request.pickupWindow)

// The shape rules of the product fields, shared by POST and PUT so each rule exists once.
private fun validateProductFields(
    name: String,
    barcode: String?,
    originalPrice: Double,
    pickupWindow: PickupWindowRequest?,
): ValidationResult {
    requireText(name, field = "name")
    // No barcode is fine (bread from a bakery); a barcode follows the same rule as the lookup.
    barcode?.let { requireBarcode(it) }

    // Money and PickupWindow refuse these values with IllegalArgumentException, which would give a 500.
    if (originalPrice < 0) {
        throw ValidationException("originalPrice must not be negative", field = "originalPrice")
    }
    if (pickupWindow != null && pickupWindow.from >= pickupWindow.until) {
        throw ValidationException("pickupWindow.from must be before pickupWindow.until", field = "pickupWindow")
    }

    return ValidationResult.Valid
}

// UPC-E (7) up to GTIN-14 (14), only the digits 0-9; [0-9] instead of \d, which could also match other scripts.
private val BARCODE = Regex("[0-9]{7,14}")

/**
 * The one barcode rule, for the lookup and for POST and PUT (US-02, decision Eva 22 Oct).
 * In the lookup the barcode becomes part of the URL to Open Food Facts, so this is a trust boundary.
 *
 * @throws ValidationException when [barcode] is not 7 to 14 digits; StatusPages turns it into a 400.
 */
internal fun requireBarcode(barcode: String) {
    if (!BARCODE.matches(barcode)) {
        throw ValidationException("barcode must be 7 to 14 digits", field = "barcode")
    }
}

// A blank text counts as empty: a name of only spaces is no name.
private fun requireText(value: String, field: String) {
    if (value.isBlank()) throw ValidationException("$field must not be empty", field = field)
    if (value.length > MAX_TEXT_LENGTH) {
        throw ValidationException("$field can be at most $MAX_TEXT_LENGTH characters", field = field)
    }
}
