package com.restpartijen.api.shared

/**
 * Current price of a product, based on the discount tiers of its category (K-3, K-4).
 * Provided by: F3 (Lonneke), implemented by PricingService with an injected Clock.
 * Used by: F1 (Eva) for the product detail, F2 (Stefan) for search and reservation.
 *
 * Both return null when the product has expired (B-18).
 */
interface PriceProvider {
    /** The price of [product] now, after its discount; null when it has expired. */
    fun currentPrice(product: PricedProduct): Money?

    /** The original price, discount and current price of [product] now; null when it has expired. */
    fun priceBreakdown(product: PricedProduct): PriceBreakdown?
}
