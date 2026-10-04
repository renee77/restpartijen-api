package com.restpartijen.api.shared

/**
 * The only way for other features to change a product's status (K-1, K-6).
 * Provided by: F1 (Eva). Used by: F2 (Stefan) for reservations, F3 (Lonneke) for expiry and removal.
 */
interface ProductStatusUpdater {

    /** Sets RESERVED only if the product is still LISTED. Returns false if someone else was first. */
    suspend fun markReserved(productId: Long): Boolean

    // Nog bespreken: markListed en markCollected gaven Unit terug. De aanroeper zag dus niet of de
    // overgang lukte, bijvoorbeeld markCollected op een partij die niet RESERVED is. Nu geven ze,
    // net als markReserved en markRemoved, false terug als de partij niet in de verwachte status was.
    // Stefan (F2) en Lonneke (F3) moeten die false afhandelen, bijvoorbeeld met een 409.

    /** Sets RESERVED back to LISTED (reservation cancelled or lapsed). Returns false if it was not RESERVED. */
    suspend fun markListed(productId: Long): Boolean

    /** Sets RESERVED to COLLECTED. Returns false if it was not RESERVED. */
    suspend fun markCollected(productId: Long): Boolean

    /** Sets the given LISTED products to EXPIRED. Returns how many changed. */
    suspend fun markExpired(productIds: List<Long>): Int

    /** Sets a LISTED product to REMOVED (B-19). Returns false if it was not LISTED. */
    suspend fun markRemoved(productId: Long): Boolean
}