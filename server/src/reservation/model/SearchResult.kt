package com.restpartijen.api.reservation.model

import com.restpartijen.api.shared.PriceBreakdown
import com.restpartijen.api.shared.ProductView
import kotlin.time.Duration

/** One search hit: a product together with its current price (US-04). */
data class SearchResult(
    val product: ProductView,
    val price: PriceBreakdown,
    val shelfLifeRemaining: Duration
)