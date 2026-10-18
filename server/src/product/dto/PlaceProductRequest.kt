package com.restpartijen.api.product.dto

import com.restpartijen.api.product.service.PlaceProductCommand
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ValidationException
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Body of POST /api/v1/products (US-01).
 *
 * The price is in euros, for example 3.49 (B-25). Times are ISO-8601 text, for example "2026-10-04T00:00:00Z".
 * An allergen that is not one of the fourteen EU allergens cannot be read, which gives a 400 (§5.1).
 * There is no supplier field: the supplier comes from the token (§16.2).
 */
@Serializable
data class PlaceProductRequest(
    val category: ProductCategory,
    val name: String,
    val originalPrice: Double,
    val bestBeforeAt: Instant,
    val quantity: Int,
    val barcode: String? = null,
    val pickupWindow: PickupWindowRequest? = null,
    val allergens: Set<Allergen> = emptySet(),
)

/** The pickup window as it arrives over the network; checked when it becomes a [PickupWindow]. */
@Serializable
data class PickupWindowRequest(val from: Instant, val until: Instant)

/**
 * Turns the request into a command in domain types, and checks its shape on the way.
 *
 * Money and PickupWindow refuse wrong values with IllegalArgumentException, which StatusPages would answer
 * with a 500. Checking here first gives a 400 with the field instead. Domain rules, such as a date in the past,
 * are not checked here: they belong to the product (validateForListing, 422).
 */
fun PlaceProductRequest.toCommand(): PlaceProductCommand {
    if (originalPrice < 0) throw ValidationException("originalPrice must not be negative", field = "originalPrice")
    if (pickupWindow != null && pickupWindow.from >= pickupWindow.until) {
        throw ValidationException("pickupWindow.from must be before pickupWindow.until", field = "pickupWindow")
    }

    return PlaceProductCommand(
        category = category,
        name = name,
        originalPrice = Money.fromEuros(originalPrice),
        bestBeforeAt = bestBeforeAt,
        quantity = quantity,
        barcode = barcode,
        pickupWindow = pickupWindow?.let { PickupWindow(from = it.from, until = it.until) },
        allergens = allergens,
    )
}
