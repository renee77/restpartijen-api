package com.restpartijen.api.shared


// restpartijen-api/src/shared/ProductView.kt
/**
 * Read-only view of a surplus product for other features (K-1, K-6).
 * Provided by: F1 (Eva), implemented by SurplusProduct.
 * Used by: F2 (Stefan) for search and reservation, F3 (Lonneke) for expiry and admin.
 *
 * Only vals: status changes go through ProductStatusUpdater.
 * Extends PricedProduct so F2 can pass a ProductView straight to PriceProvider.
 */
interface ProductView : PricedProduct {
    val id: Long
    val supplier: SupplierSummary   // decision point 5
    val name: String
    val status: ProductStatus
    val pickupWindow: PickupWindow
}
