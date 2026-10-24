package com.restpartijen.api.shared

import kotlin.time.Clock
import kotlin.time.Duration

/**
 * What pricing needs to know about a product, and nothing more (K-2).
 * Provided by: F1 (Eva), implemented by SurplusProduct. Used by: F3 (Lonneke) via PriceProvider.
 */
interface PricedProduct {
    /** The price the supplier asked, before any discount. */
    val originalPrice: Money

    /** The product kind; it decides which discount tiers apply (§9.4). */
    val category: ProductCategory

    /** Time left until best-before; zero or negative when expired. */
    fun shelfLifeRemaining(clock: Clock): Duration
}
