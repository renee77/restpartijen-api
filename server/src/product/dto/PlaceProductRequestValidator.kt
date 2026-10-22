package com.restpartijen.api.product.dto

import com.restpartijen.api.shared.ValidationException
import io.ktor.server.plugins.requestvalidation.ValidationResult

// Matches the varchar(255) columns in ProductsTable; a longer text would fail on INSERT with a 500.
private const val MAX_TEXT_LENGTH = 255

/**
 * Checks the shape of a POST /api/v1/products body before the route uses it (§10.2, ADR-05).
 *
 * Registered in configureRequestValidation(). Throws ValidationException with the field, so StatusPages
 * answers 400 with `field` filled in (option B). Domain rules, such as a date in the past or a quantity
 * below 1, are not checked here: they belong to the product (validateForListing, 422).
 */
fun validatePlaceProductRequest(request: PlaceProductRequest): ValidationResult {
    requireText(request.name, field = "name")
    request.barcode?.let { requireText(it, field = "barcode") }

    // Money and PickupWindow refuse these values with IllegalArgumentException, which would give a 500.
    if (request.originalPrice < 0) {
        throw ValidationException("originalPrice must not be negative", field = "originalPrice")
    }
    val window = request.pickupWindow
    if (window != null && window.from >= window.until) {
        throw ValidationException("pickupWindow.from must be before pickupWindow.until", field = "pickupWindow")
    }

    return ValidationResult.Valid
}

// A blank text counts as empty: a name of only spaces is no name.
private fun requireText(value: String, field: String) {
    if (value.isBlank()) throw ValidationException("$field must not be empty", field = field)
    if (value.length > MAX_TEXT_LENGTH) {
        throw ValidationException("$field can be at most $MAX_TEXT_LENGTH characters", field = field)
    }
}
