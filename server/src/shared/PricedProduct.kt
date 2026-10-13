package com.restpartijen.api.shared

import kotlin.time.Clock
import kotlin.time.Duration

/**
 * What pricing needs to know about a product, and nothing more (K-2).
 * Provided by: F1 (Eva), implemented by SurplusProduct. Used by: F3 (Lonneke) via PriceProvider.
 */
interface PricedProduct {
    val originalPrice: Money
    val category: ProductCategory

    /** Time left until best-before; zero or negative when expired. */
    fun shelfLifeRemaining(clock: Clock): Duration
}