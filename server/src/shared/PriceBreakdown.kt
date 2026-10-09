package com.restpartijen.api.shared

/** How the current price was built up; shown in GET /products/{id} (§5.7). */
data class PriceBreakdown(
    // Money with whole cents in Long, discounts as WHOLE percentages
    val originalPrice: Money,
    val discountPercentage: Int,
    val currentPrice: Money
)