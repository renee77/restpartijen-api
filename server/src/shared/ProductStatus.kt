package com.restpartijen.api.shared

/**
 * Lifecycle status of a surplus product (§9.5).
 *
 * COLLECTED, EXPIRED and REMOVED are end states: once reached, the status never changes again.
 * Other features change the status only through [ProductStatusUpdater].
 */
enum class ProductStatus {
    /** Available: can be found in search and reserved. */
    LISTED,

    /** Held for one collector until collected, cancelled or lapsed. */
    RESERVED,

    /** Picked up by the collector. End state. */
    COLLECTED,

    /** Best-before moment passed while still listed. End state. */
    EXPIRED,

    /** Removed by its supplier or an admin. End state; the public API treats it as not found (404). */
    REMOVED,
}