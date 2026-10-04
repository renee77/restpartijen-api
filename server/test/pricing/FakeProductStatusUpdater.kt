package com.restpartijen.api.pricing

import com.restpartijen.api.shared.ProductStatusUpdater

class FakeProductStatusUpdater(private val expiredCount: Int? = null): ProductStatusUpdater {
    override suspend fun markExpired(productIds: List<Long>): Int {
        return expiredCount ?: productIds.size
    }

    override suspend fun markCollected(productId: Long) {
        TODO("Not needed for the expiry tests")
    }

    override suspend fun markListed(productId: Long) {
        TODO("Not needed for the expiry tests")
    }

    override suspend fun markRemoved(productId: Long): Boolean {
        TODO("Not needed for the expiry tests")
    }

    override suspend fun markReserved(productId: Long): Boolean {
        TODO("Not needed for the expiry tests")
    }
}