package com.restpartijen.api.pricing

import com.restpartijen.api.shared.ReservationMaintenance
import kotlin.time.Instant

/** Fake of F2's ReservationMaintenance (K-5): returns a fixed number, no database. */
class FakeReservationMaintenance(
    private val lapsedCount: Int = 0,
) : ReservationMaintenance {

    override suspend fun lapseOverdue(now: Instant): Int = lapsedCount

    // Not used by the expiry task (only by US-09); fails loudly if it is called anyway.
    override suspend fun lapseActiveFor(productId: Long): Boolean =
        TODO("not needed for the expiry tests")
}