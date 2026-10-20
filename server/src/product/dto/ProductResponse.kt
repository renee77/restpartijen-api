package com.restpartijen.api.product.dto

import com.restpartijen.api.persistence.ProductsTable.supplierId
import com.restpartijen.api.product.model.SurplusProduct
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.PickupWindow
import kotlinx.serialization.Serializable

/**
 * JSON response for a single product (US-01, TC-01).
 *
 * Kept separate from the domain model so the API contract can change
 * without touching SurplusProduct, and vice versa.
 * The price is in euros (B-25); times are ISO-8601 text, like ReservationResponse.
 * Only the supplier id: the client needs nothing more about the supplier here.
 */
@Serializable
data class ProductResponse(
    val id: Long,
    val supplierId: Long,
    val category: String,
    val name: String,
    val barcode: String?,
    val originalPrice: Double,
    val bestBeforeAt: String,
    val quantity: Int,
    val status: String,
    val pickupWindow: PickupWindowResponse,
    val allergens: Set<Allergen>,
)

/** The pickup window in a response; the counterpart of PickupWindowRequest. */
@Serializable
data class PickupWindowResponse(val from: String, val until: String)

/** Maps the domain model to the API response. */
fun SurplusProduct.toResponse(): ProductResponse = ProductResponse(
    id = id,
    name = name,
    category = category.name,
    supplierId = supplier.id,
    barcode = barcode,
    originalPrice = originalPrice.toEuros(),
    bestBeforeAt = bestBeforeAt.toString(),
    quantity = quantity,
    status = status.name,
    pickupWindow = PickupWindowResponse(from = pickupWindow.from.toString(), until = pickupWindow.until.toString()),
    allergens = allergens,
)