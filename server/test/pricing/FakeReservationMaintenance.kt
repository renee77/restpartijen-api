package com.restpartijen.api.pricing

import com.restpartijen.api.shared.ReservationMaintenance
import kotlin.time.Instant

/**
 * Fake of F2's ReservationMaintenance (K-5): fixed answers, no database.
 * Every call is written to [calls]; share the list with FakeProductStatusUpdater to check the order.
 */
class FakeReservationMaintenance(
    private val lapsedCount: Int = 0,
    private val lapseActiveSucceeds: Boolean = true,
    val calls: MutableList<String> = mutableListOf(),
) : ReservationMaintenance {

    override suspend fun lapseOverdue(now: Instant): Int {
        calls += "lapseOverdue"
        return lapsedCount
    }

    override suspend fun lapseActiveFor(productId: Long): Boolean {
        calls += "lapseActiveFor($productId)"
        return lapseActiveSucceeds
    }
}