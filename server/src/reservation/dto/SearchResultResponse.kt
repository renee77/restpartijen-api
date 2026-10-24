package com.restpartijen.api.reservation.dto

import com.restpartijen.api.reservation.model.SearchResult
import kotlinx.serialization.Serializable

/** One product in the search result (US-04). Prices in euros (B-25); the supplier's coordinates let the app compute the distance (§18.1). */
@Serializable
data class SearchResultResponse(
    val id: Long,
    val name: String,
    val category: String,
    val supplier: SupplierResponse,
    val originalPrice: Double,
    val discountPercentage: Int,
    val currentPrice: Double,
    val shelfLifeRemainingHours: Long,
    val pickupWindow: PickupWindowResponse,
)

/**
 * The pickup window, in the same shape as in F1's ProductResponse: { "from": ..., "until": ... }.
 * Its own class because features only share code through shared (GI-5); the JSON is what must match.
 */
@Serializable
data class PickupWindowResponse(val from: String, val until: String)

/** Name and location of the supplier, without personal data (§15). */
@Serializable
data class SupplierResponse(val id: Long, val name: String, val latitude: Double, val longitude: Double)

fun SearchResult.toResponse() = SearchResultResponse(
    id = product.id,
    name = product.name,
    category = product.category.name,
    supplier = SupplierResponse(
        id = product.supplier.id,
        name = product.supplier.name,
        latitude = product.supplier.latitude,
        longitude = product.supplier.longitude,
    ),
    originalPrice = price.originalPrice.toEuros(),
    discountPercentage = price.discountPercentage,
    currentPrice = price.currentPrice.toEuros(),
    shelfLifeRemainingHours = shelfLifeRemaining.inWholeHours,
    pickupWindow = PickupWindowResponse(
        from = product.pickupWindow.from.toString(),
        until = product.pickupWindow.until.toString(),
    ),
)
