package com.restpartijen.api.product.service

import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

// Open Food Facts allows 15 product reads per minute per IP and may block an IP that goes over (§10.3);
// 10 leaves room for a retry or a second instance (decision Eva, 22 Oct).
private const val MAX_CALLS = 10
private val WINDOW = 1.minutes

/**
 * Counts the calls to Open Food Facts over the last minute, for the whole application (security review F-01).
 * Above the limit the lookup gives manual entry instead of calling out, so a loop or a stolen token
 * cannot get the IP blocked.
 *
 * @param clock decides when a call has left the window (GI-6)
 */
class OpenFoodFactsRateLimiter(private val clock: Clock) {

    // Times of the allowed calls, oldest first.
    private val calls = ArrayDeque<Instant>()

    /** True, and counted, when a call may go out now; false, and not counted, when 10 went out this minute. */
    fun tryAcquire(): Boolean = synchronized(calls) {
        val now = clock.now()
        while (calls.isNotEmpty() && now - calls.first() >= WINDOW) {
            calls.removeFirst()
        }
        if (calls.size < MAX_CALLS) {
            calls.addLast(now)
            true
        } else {
            false
        }
    }
}
