package com.restpartijen.api.shared

/**
 * How the current price was built up; shown in GET /products/{id} (§5.7).
 *
 * Amounts are [Money] in whole cents, the discount is a whole percentage (ADR-07).
 * The class guards itself, like PickupWindow does.
 *
 * @property originalPrice the price the supplier asked
 * @property discountPercentage the discount of the current tier, from 0 to 100
 * @property currentPrice the price after the discount; never above [originalPrice]
 */
data class PriceBreakdown(val originalPrice: Money, val discountPercentage: Int, val currentPrice: Money) {
    init {
        require(discountPercentage in 0..100) { "Discount must be between 0 and 100" }
        require(currentPrice.cents <= originalPrice.cents) {
            "Current price cannot exceed the original price"
        }
    }
}
