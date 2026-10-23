package com.restpartijen.api.reservation.model

import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.ProductCategory
import kotlin.time.Duration

/** The filters of a search (US-04, §5.4). Null means: do not filter on this. All filters apply together (AND). */
data class SearchCriteria(
    val category: ProductCategory? = null,
    val maxPrice: Money? = null,
    val minShelfLife: Duration? = null,
)