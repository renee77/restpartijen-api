package com.restpartijen.api.product.service

import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

// Allergens on Open Food Facts rarely change; after a day a correction there still comes through (decision Eva, 22 Oct).
private val TIME_TO_LIVE = 24.hours

// Enough for every barcode in a demo or a busy day, small enough to never fill the memory.
private const val MAX_BARCODES = 1_000

/**
 * Remembers found products per barcode, so the lookup stays under the rate limit of Open Food Facts (§10.3).
 * Only found products go in; a time-out or rate limit is not remembered, or the failure would stick.
 *
 * @param clock decides when an entry has expired (GI-6)
 */
class ProductLookupCache(private val clock: Clock) {

    // Keeps the order in which barcodes came in, so the first key is always the oldest.
    private val entries = LinkedHashMap<String, Entry>()

    /** The cached product for [barcode], or null when there is none or it is 24 hours or older. */
    fun get(barcode: String): ProductLookup? = synchronized(entries) {
        val entry = entries[barcode]
        when {
            entry == null -> null
            // Expired entries are removed when someone asks for them; MAX_BARCODES keeps the rest bounded.
            clock.now() >= entry.storedAt + TIME_TO_LIVE -> {
                entries.remove(barcode)
                null
            }
            else -> entry.lookup
        }
    }

    /** Stores [lookup] for [barcode]; when 1,000 barcodes are stored, the oldest goes out first. */
    fun put(barcode: String, lookup: ProductLookup) {
        synchronized(entries) {
            // Removed first, so a barcode that is stored again moves to the back as the newest.
            entries.remove(barcode)
            if (entries.size >= MAX_BARCODES) {
                entries.remove(entries.keys.first())
            }
            entries[barcode] = Entry(lookup, clock.now())
        }
    }

    private data class Entry(val lookup: ProductLookup, val storedAt: Instant)
}
