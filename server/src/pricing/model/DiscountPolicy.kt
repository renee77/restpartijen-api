package com.restpartijen.api.pricing.model

import com.restpartijen.api.shared.ProductCategory
import kotlin.time.Duration

/**
 * Discount rules for one product category.
 *
 * Each implementation only supplies its own staffels through [tiers].
 * The lookup logic lives here, in the default implementations,
 * so it exists in exactly one place.
 */
interface DiscountPolicy {

    /** The product category this policy is responsible for. */
    fun appliesTo(): ProductCategory

    /** The staffels of this category, in any order. */
    fun tiers(): List<DiscountTier>

    /**
     * The discount percentage for the given remaining shelf life.
     * On a boundary the higher discount applies.
     * Returns 0 when no staffel applies yet.
     */
    fun discountPercentage(remaining: Duration): Int =
        tiers().tierFor(remaining)?.percentage ?: 0

    /**
     * The highest discount percentage this policy can ever give.
     * Returns 0 when the policy has no staffels.
     */
    fun maxDiscount(): Int =
        tiers().maxOfOrNull { it.percentage } ?: 0
}