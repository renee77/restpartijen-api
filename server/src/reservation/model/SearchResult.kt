package com.restpartijen.api.reservation.model

import com.restpartijen.api.shared.PriceBreakdown
import com.restpartijen.api.shared.ProductView
import kotlin.time.Duration

/**
 * One search hit: a product together with its current price (US-04).
 *
 * @property product the product as F1 shows it
 * @property price the price breakdown at the moment of searching
 * @property shelfLifeRemaining the shelf life left at the moment of searching, used to filter and sort
 */
data class SearchResult(val product: ProductView, val price: PriceBreakdown, val shelfLifeRemaining: Duration)
