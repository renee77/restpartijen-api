package com.restpartijen.api.shared

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
    /** Name and coordinates of the supplier, so a search result can show them (§5.4, B-20). */
    val supplier: SupplierSummary
    val name: String
    val status: ProductStatus
    val pickupWindow: PickupWindow
}
