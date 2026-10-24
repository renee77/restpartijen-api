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
    /** The id the database gave the product. */
    val id: Long

    /** Name and coordinates of the supplier, so a search result can show them (§5.4, B-20). */
    val supplier: SupplierSummary

    /** The product name the supplier entered. */
    val name: String

    /** The stored lifecycle status; a LISTED product past its best-before stays LISTED until expiry runs. */
    val status: ProductStatus

    /** When the product can be collected, derived from the supplier's opening hours (US-10). */
    val pickupWindow: PickupWindow
}
