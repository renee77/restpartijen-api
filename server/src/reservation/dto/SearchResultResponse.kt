package com.restpartijen.api.reservation.dto

import com.restpartijen.api.reservation.model.SearchResult
import kotlinx.serialization.Serializable

/**
 * One product in the search result (US-04). Prices in euros (B-25); the supplier's coordinates let the app compute
 * the distance (§18.1).
 *
 * @property id the product id
 * @property name the product name
 * @property category the product category, as the enum name
 * @property supplier name and location of the supplier
 * @property originalPrice the price without discount, in euros
 * @property discountPercentage the discount that applies now
 * @property currentPrice the price after discount, in euros
 * @property shelfLifeRemainingHours whole hours left until the best-before moment, rounded down
 * @property pickupWindow when the product can be collected
 */
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
 *
 * @property from start of the pickup window, as an ISO-8601 instant
 * @property until end of the pickup window, as an ISO-8601 instant
 */
@Serializable
data class PickupWindowResponse(val from: String, val until: String)

/**
 * Name and location of the supplier, without personal data (§15).
 *
 * @property id the supplier id
 * @property name the supplier name
 * @property latitude the supplier's latitude, so the app can compute the distance
 * @property longitude the supplier's longitude, so the app can compute the distance
 */
@Serializable
data class SupplierResponse(val id: Long, val name: String, val latitude: Double, val longitude: Double)

/** Maps a search hit to its JSON answer: prices in euros and the moments as ISO-8601 text. */
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
