package com.restpartijen.api.reservation.model

import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.ProductCategory
import kotlin.time.Duration

/**
 * The filters of a search (US-04, §5.4). Null means: do not filter on this. All filters apply together (AND).
 *
 * @property category only products of this category
 * @property maxPrice only products whose current, discounted price is at most this amount
 * @property minShelfLife only products with at least this much shelf life left
 */
data class SearchCriteria(
    val category: ProductCategory? = null,
    val maxPrice: Money? = null,
    val minShelfLife: Duration? = null,
)
