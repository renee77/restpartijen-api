package com.restpartijen.api.pricing.model

import com.restpartijen.api.shared.ProductCategory
import kotlin.time.Duration.Companion.hours

/**
 * Discount policy for fresh products: discounts start two days before the
 * best-before date and rise in three steps to 60%.
 */
class FreshDiscountPolicy : DiscountPolicy {
    /** 10% from 48 hours before the best-before date, 40% from 24 hours, 60% from 12 hours. */
    private val discountTierList = listOf(
        DiscountTier(48.hours, 10),
        DiscountTier(24.hours, 40),
        DiscountTier(12.hours, 60),
    )

    override fun appliesTo(): ProductCategory = ProductCategory.FRESH

    override fun tiers(): List<DiscountTier> = discountTierList
}
