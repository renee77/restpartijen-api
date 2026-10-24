package com.restpartijen.api.pricing.model

import kotlin.time.Duration

data class DiscountTier(val appliesFrom: Duration, val percentage: Int)

/**
 * Finds the tier that applies for the given remaining shelf life.
 * A tier applies when [remaining] is equal to or less than its appliesFrom,
 * so on a boundary the higher discount applies.
 * Returns null when no tier applies yet. The order of the list does not matter.
 */
fun List<DiscountTier>.tierFor(remaining: Duration): DiscountTier? = this
    .filter { remaining <= it.appliesFrom }
    .minByOrNull { it.appliesFrom }
