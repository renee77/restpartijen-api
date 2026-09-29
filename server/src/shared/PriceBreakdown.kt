package com.restpartijen.api.shared

/** How the current price was built up; shown in GET /products/{id} (§5.7). */
data class PriceBreakdown(
    // Money met hele centen in een Long. Kortingen als HELE percentages (INT).
    val originalPrice: Money,
    val discountPercentage: Int,
    val currentPrice: Money
)