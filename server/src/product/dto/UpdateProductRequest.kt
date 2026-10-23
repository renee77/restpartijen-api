package com.restpartijen.api.product.dto

import com.restpartijen.api.product.service.UpdateProductCommand
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Body of PUT /api/v1/products/{id} (US-03, A13): every field of POST except category.
 *
 * PUT replaces the product, so the body is complete; the same units as POST (price in euros, bestBeforeAt as an
 * ISO-8601 date, other times as ISO-8601 text).
 * A body with a category is refused with a 400: the JSON reader does not accept unknown fields.
 * There is no supplier field: the owner is checked against the token (§16.2).
 */
@Serializable
data class UpdateProductRequest(
    val name: String,
    val originalPrice: Double,
    val bestBeforeAt: LocalDate,
    val quantity: Int,
    val barcode: String? = null,
    val pickupWindow: PickupWindowRequest? = null,
    val allergens: Set<Allergen> = emptySet(),
)

/**
 * Turns the request into a command in domain types; it only translates.
 *
 * The shape was already checked by validateUpdateProductRequest in RequestValidation (400), so Money and
 * PickupWindow receive valid values here.
 */
fun UpdateProductRequest.toCommand(): UpdateProductCommand {
    return UpdateProductCommand(
        name = name,
        originalPrice = Money.fromEuros(originalPrice),
        bestBeforeAt = bestBeforeAt.toBestBeforeAt(),
        quantity = quantity,
        barcode = barcode,
        pickupWindow = pickupWindow?.let { PickupWindow(from = it.from, until = it.until) },
        allergens = allergens,
    )
}
