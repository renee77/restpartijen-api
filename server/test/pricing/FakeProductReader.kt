package com.restpartijen.api.pricing

import com.restpartijen.api.shared.ProductReader
import com.restpartijen.api.shared.ProductStatus
import kotlin.time.Instant
import com.restpartijen.api.shared.ProductView

class FakeProductReader(
    private val expiredListings: List<ProductView> = emptyList(),
    private val products: List<ProductView> = emptyList(),
    ) : ProductReader {
    override suspend fun findById(id: Long): ProductView? {
        TODO("Not needed for the expiry tests")
    }
    override suspend fun findAvailable(now: Instant): List<ProductView> {
        TODO("Not needed for the expiry tests")
    }
    override suspend fun findByStatus(statuses: Set<ProductStatus>): List<ProductView> {
        return products
    }

    override suspend fun findExpiredListings(now: Instant): List<ProductView> {
        return expiredListings
    }
}