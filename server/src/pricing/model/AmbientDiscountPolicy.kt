package com.restpartijen.api.pricing.model

import com.restpartijen.api.shared.ProductCategory
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * Discount policy for ambient products: discounts start fourteen days before the
 * best-before date and rise in two steps to 40%.
 */
class AmbientDiscountPolicy : DiscountPolicy {
    /** 15% from fourteen days before the best-before date, 40% from seven days before. */
    private val discountTierList = listOf(
        DiscountTier(14.days, 15),
        DiscountTier(7.days, 40),
    )

    override fun appliesTo(): ProductCategory = ProductCategory.AMBIENT

    override fun tiers(): List<DiscountTier> = discountTierList
}
