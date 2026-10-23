package com.restpartijen.api.product.dto

import com.restpartijen.api.product.service.PlaceProductCommand
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductCategory
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Body of POST /api/v1/products (US-01).
 *
 * The price is in euros, for example 3.49 (B-25). bestBeforeAt is an ISO-8601 date, for example "2026-10-04",
 * because a label shows a date; a time gives a 400. Other times are ISO-8601 text, for example
 * "2026-10-01T09:00:00Z".
 * An allergen that is not one of the fourteen EU allergens cannot be read, which gives a 400 (§5.1).
 * There is no supplier field: the supplier comes from the token (§16.2).
 */
@Serializable
data class PlaceProductRequest(
    val category: ProductCategory,
    val name: String,
    val originalPrice: Double,
    val bestBeforeAt: LocalDate,
    val quantity: Int,
    val barcode: String? = null,
    val pickupWindow: PickupWindowRequest? = null,
    val allergens: Set<Allergen> = emptySet(),
)

/** The pickup window as it arrives over the network; checked by validatePlaceProductRequest. */
@Serializable
data class PickupWindowRequest(val from: Instant, val until: Instant)

/**
 * Turns the request into a command in domain types; it only translates.
 *
 * The shape was already checked by validatePlaceProductRequest in RequestValidation (400), so Money and
 * PickupWindow receive valid values here.
 */
fun PlaceProductRequest.toCommand(): PlaceProductCommand {
    return PlaceProductCommand(
        category = category,
        name = name,
        originalPrice = Money.fromEuros(originalPrice),
        bestBeforeAt = bestBeforeAt.toBestBeforeAt(),
        quantity = quantity,
        barcode = barcode,
        pickupWindow = pickupWindow?.let { PickupWindow(from = it.from, until = it.until) },
        allergens = allergens,
    )
}
