package com.restpartijen.api.pricing

import com.restpartijen.api.shared.ProductReader
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductView
import kotlin.time.Instant

/**
 * Fake of F1's ProductReader: lists in memory, and it remembers which statuses were asked for.
 *
 * @property expiredListings what findExpiredListings returns, whatever the moment
 * @property products the products that findById, findAvailable and findByStatus search in
 */
class FakeProductReader(
    private val expiredListings: List<ProductView> = emptyList(),
    private val products: List<ProductView> = emptyList(),
) : ProductReader {

    /** The set given to the last findByStatus call, or null if it was never called. */
    var requestedStatuses: Set<ProductStatus>? = null
        private set

    // Like the real contract: a REMOVED product counts as not found (B-32)
    override suspend fun findById(id: Long): ProductView? =
        products.firstOrNull { it.id == id && it.status != ProductStatus.REMOVED }

    override suspend fun findAvailable(now: Instant): List<ProductView> =
        products.filter { it.status == ProductStatus.LISTED }

    override suspend fun findByStatus(statuses: Set<ProductStatus>): List<ProductView> {
        requestedStatuses = statuses
        return products.filter { it.status in statuses }
    }

    override suspend fun findExpiredListings(now: Instant): List<ProductView> = expiredListings
}
