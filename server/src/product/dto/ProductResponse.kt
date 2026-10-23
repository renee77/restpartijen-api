package com.restpartijen.api.product.dto

import com.restpartijen.api.product.service.ProductDetail
import com.restpartijen.api.shared.Allergen
import kotlinx.serialization.Serializable

/**
 * JSON response for a single product (US-01, TC-01).
 *
 * Kept separate from the domain model so the API contract can change
 * without touching SurplusProduct, and vice versa.
 * The price is in euros (B-25); times are ISO-8601 text, like ReservationResponse.
 * Only the supplier id: the client needs nothing more about the supplier here.
 * discountPercentage and currentPrice come from PriceProvider (§5.7, §10.1); both are null when expired (B-18).
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
    val discountPercentage: Int?,
    val currentPrice: Double?,
)

/** The pickup window in a response; the service derives it, so there is no request counterpart (US-10). */
@Serializable
data class PickupWindowResponse(val from: String, val until: String)

/**
 * Maps a product with its price to the API response; it only translates.
 * The status comes from the detail, not from the product, so an expired LISTED product shows as EXPIRED.
 */
fun ProductDetail.toResponse(): ProductResponse = ProductResponse(
    id = product.id,
    name = product.name,
    category = product.category.name,
    supplierId = product.supplier.id,
    barcode = product.barcode,
    originalPrice = product.originalPrice.toEuros(),
    bestBeforeAt = product.bestBeforeAt.toString(),
    quantity = product.quantity,
    status = status.name,
    pickupWindow = PickupWindowResponse(
        from = product.pickupWindow.from.toString(),
        until = product.pickupWindow.until.toString(),
    ),
    allergens = product.allergens,
    discountPercentage = price?.discountPercentage,
    currentPrice = price?.currentPrice?.toEuros(),
)
