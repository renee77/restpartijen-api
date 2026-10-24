package com.restpartijen.api.product.dto

import com.restpartijen.api.product.service.UpdateProductCommand
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Body of PUT /api/v1/products/{id} (US-03, A13): every field of POST except category.
 *
 * PUT replaces the product, so the body is complete; the same units as POST (price in euros, bestBeforeAt as an
 * ISO-8601 date). There is no pickup window: the service derives it again (US-10).
 * A body with a category is refused with a 400: the JSON reader does not accept unknown fields.
 * There is no supplier field: the owner is checked against the token (§16.2).
 *
 * @property name the product name, not blank and at most MAX_TEXT_LENGTH characters
 * @property originalPrice the price in euros before any discount
 * @property bestBeforeAt the best-before date on the label
 * @property quantity how many units are offered
 * @property barcode the barcode on the package, 7 to 14 digits, or null for a product without one
 * @property allergens the EU allergens the product contains; empty when none are given
 */
@Serializable
data class UpdateProductRequest(
    val name: String,
    val originalPrice: Double,
    val bestBeforeAt: LocalDate,
    val quantity: Int,
    val barcode: String? = null,
    val allergens: Set<Allergen> = emptySet(),
)

/**
 * Turns the request into a command in domain types; it only translates.
 *
 * The shape was already checked by validateUpdateProductRequest in RequestValidation (400), so Money
 * receives a valid value here.
 */
fun UpdateProductRequest.toCommand(): UpdateProductCommand = UpdateProductCommand(
    name = name,
    originalPrice = Money.fromEuros(originalPrice),
    bestBeforeAt = bestBeforeAt.toBestBeforeAt(),
    quantity = quantity,
    barcode = barcode,
    allergens = allergens,
)
