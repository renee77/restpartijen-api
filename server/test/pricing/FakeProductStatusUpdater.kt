package com.restpartijen.api.pricing

import com.restpartijen.api.shared.ProductStatusUpdater

/**
 * Fake of F1's ProductStatusUpdater. Every call is written to [calls], so a test can check
 * the order. Share one list with FakeReservationMaintenance to see the order across both.
 */
class FakeProductStatusUpdater(
    private val expiredCount: Int? = null,
    private val removeSucceeds: Boolean = true,
    val calls: MutableList<String> = mutableListOf(),
) : ProductStatusUpdater {

    /** The ids that markExpired was asked to expire. */
    val expiredIds = mutableListOf<Long>()

    /** The ids that markRemoved successfully removed. */
    val removedIds = mutableListOf<Long>()

    override suspend fun markExpired(productIds: List<Long>): Int {
        calls += "markExpired"
        expiredIds += productIds
        return expiredCount ?: productIds.size
    }

    override suspend fun markRemoved(productId: Long): Boolean {
        calls += "markRemoved($productId)"
        if (removeSucceeds) removedIds += productId
        return removeSucceeds
    }

    override suspend fun markCollected(productId: Long): Boolean {
        TODO("Not needed for the pricing tests")
    }

    override suspend fun markListed(productId: Long): Boolean {
        TODO("Not needed for the pricing tests")
    }

    override suspend fun markReserved(productId: Long): Boolean {
        TODO("Not needed for the pricing tests")
    }
}