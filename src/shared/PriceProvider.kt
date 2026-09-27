package com.restpartijen.api.shared

/**
 * Current price of a product, based on the discount tiers of its category (K-3, K-4).
 * Provided by: F3 (Lonneke), implemented by PricingService with an injected Clock.
 * Used by: F1 (Eva) for the product detail, F2 (Stefan) for search and reservation.
 *
 * Both return null when the product has expired (decision point 2).
 */
interface PriceProvider {
    fun currentPrice(product: PricedProduct): Money?
    fun priceBreakdown(product: PricedProduct): PriceBreakdown?
}