package com.restpartijen.api.reservation.service

import com.restpartijen.api.reservation.model.SearchCriteria
import com.restpartijen.api.reservation.model.SearchResult
import com.restpartijen.api.shared.PriceProvider
import com.restpartijen.api.shared.ProductReader
import kotlin.time.Clock

/**
 * Searching the offer (US-04, §5.4, SD-2). Talks to other features only through the contracts in shared (GI-5).
 */
class ProductSearchService(
    private val productReader: ProductReader,
    private val priceProvider: PriceProvider,
    private val clock: Clock,
) {
    /** LISTED products that are not expired, filtered on [criteria] (AND), soonest best-before first. */
    suspend fun search(criteria: SearchCriteria): List<SearchResult> = productReader.findAvailable(clock.now())
        // A product without a price expired just now (B-18): leave it out, it is not an error
        .mapNotNull { product ->
            priceProvider.priceBreakdown(product)?.let { price ->
                SearchResult(product, price, product.shelfLifeRemaining(clock))
            }
        }
        .filter { criteria.category == null || it.product.category == criteria.category }
        // Compares with the current, discounted price, not the original one (§5.4)
        .filter { criteria.maxPrice == null || it.price.currentPrice.cents <= criteria.maxPrice.cents }
        .filter { criteria.minShelfLife == null || it.shelfLifeRemaining >= criteria.minShelfLife }
        .sortedBy { it.shelfLifeRemaining }
}
