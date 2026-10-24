package com.restpartijen.api.product.service

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Remembers values per key for [timeToLive], and at most [maxEntries] keys; when full, the oldest goes out first.
 * The one mechanism behind both lookup caches, so a change to it happens in one place (code review, round 2).
 *
 * Nothing empties it in the background: an expired entry is removed when someone asks for it,
 * and [maxEntries] keeps the rest bounded. Safe to use from requests that run at the same time.
 *
 * @param V the type of the stored values
 * @property clock decides when an entry has expired (GI-6)
 * @property timeToLive how long an entry stays valid after it was stored
 * @property maxEntries the most keys the cache holds at once
 */
class ExpiringCache<V : Any>(private val clock: Clock, private val timeToLive: Duration, private val maxEntries: Int) {
    /**
     * The stored entries per key.
     * A LinkedHashMap keeps the order in which keys came in, so the first key is always the oldest.
     */
    private val entries = LinkedHashMap<String, Entry<V>>()

    /** The value for [key], or null when there is none or it is [timeToLive] or older. */
    fun get(key: String): V? = synchronized(entries) {
        val entry = entries[key]
        when {
            entry == null -> null

            clock.now() >= entry.storedAt + timeToLive -> {
                entries.remove(key)
                null
            }

            else -> entry.value
        }
    }

    /** Stores [value] for [key] as the newest entry; when full, the oldest goes out first. */
    fun put(key: String, value: V) {
        synchronized(entries) {
            // Removed first, so a key that is stored again moves to the back as the newest.
            entries.remove(key)
            if (entries.size >= maxEntries) {
                entries.remove(entries.keys.first())
            }
            entries[key] = Entry(value, clock.now())
        }
    }

    /**
     * One stored value.
     *
     * @property value the stored value
     * @property storedAt when it was stored; the entry expires [timeToLive] later
     */
    private data class Entry<V>(val value: V, val storedAt: Instant)
}
