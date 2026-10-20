package com.restpartijen.api.product.service

import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductCategory
import kotlin.time.Instant

/**
 * Everything ProductService needs to place a product (US-01).
 *
 * Holds domain types only: the route turns the JSON request into this command and checks its shape there,
 * so the service only deals with domain rules. No id, status or supplier: the database chooses the id,
 * a new product is always LISTED, and the supplier comes from the token, never from the request (§16.2).
 *
 * Barcode, pickup window and allergens are optional (§11.1). Without a pickup window, the service would
 * derive it from the supplier's opening hours (US-10); until those exist, it refuses the request.
 */
data class PlaceProductCommand(
    val category: ProductCategory,
    val name: String,
    val originalPrice: Money,
    val bestBeforeAt: Instant,
    val quantity: Int,
    val barcode: String? = null,
    val pickupWindow: PickupWindow? = null,
    val allergens: Set<Allergen> = emptySet(),
)
