package com.restpartijen.api.shared

import kotlin.time.Instant

/**
 * Lets the expiry job lapse reservations without knowing F2's tables (K-5).
 * Provided by: F2 (Stefan). Used by: F3 (Lonneke).
 *
 * F3 decides when; F2 decides which reservations and carries it out through its state machine.
 */
interface ReservationMaintenance {

    /**
     * Lapses every active reservation that is overdue at [now]:
     * pickup window ended, or older than 24 hours (§5.8). Returns how many lapsed.
     */
    suspend fun lapseOverdue(now: Instant): Int

    /** Lapses the active reservation of one product, before an admin removes it (US-09, decision point 3). */
    suspend fun lapseActiveFor(productId: Long): Boolean
}