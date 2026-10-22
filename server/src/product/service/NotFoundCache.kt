package com.restpartijen.api.product.service

import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

// Short, because someone may add the product to Open Food Facts; long enough to absorb retries (decision Eva, 22 Oct).
private val TIME_TO_LIVE = 60.seconds

// The same bound as ProductLookupCache, so a stream of unknown barcodes can never fill the memory.
private const val MAX_BARCODES = 1_000

/**
 * Remembers barcodes Open Food Facts did not know, so a retry gives 404 without calling out again
 * (security review F-01). Kept apart from ProductLookupCache: other lifetime, and nothing to store but the barcode.
 *
 * @param clock decides when a barcode is forgotten (GI-6)
 */
class NotFoundCache(private val clock: Clock) {

    // When each barcode was remembered, oldest first, so the first key is always the oldest.
    private val rememberedAt = LinkedHashMap<String, Instant>()

    /** True when Open Food Facts did not know [barcode] less than 60 seconds ago. */
    fun contains(barcode: String): Boolean = synchronized(rememberedAt) {
        val at = rememberedAt[barcode]
        when {
            at == null -> false
            clock.now() >= at + TIME_TO_LIVE -> {
                rememberedAt.remove(barcode)
                false
            }
            else -> true
        }
    }

    /** Remembers that Open Food Facts did not know [barcode]; when 1,000 are remembered, the oldest goes out first. */
    fun remember(barcode: String) {
        synchronized(rememberedAt) {
            rememberedAt.remove(barcode)
            if (rememberedAt.size >= MAX_BARCODES) {
                rememberedAt.remove(rememberedAt.keys.first())
            }
            rememberedAt[barcode] = clock.now()
        }
    }
}
