package com.restpartijen.api.pricing.model

import com.restpartijen.api.shared.ProductCategory
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * Discount policy for frozen products: discounts start seven days before the
 * best-before date and rise in two steps to 40%.
 */
class FrozenDiscountPolicy : DiscountPolicy {
    /** 15% from seven days before the best-before date, 40% from 24 hours before. */
    private val discountTierList = listOf(
        DiscountTier(7.days, 15),
        DiscountTier(24.hours, 40),
    )

    override fun appliesTo(): ProductCategory = ProductCategory.FROZEN

    override fun tiers(): List<DiscountTier> = discountTierList
}
