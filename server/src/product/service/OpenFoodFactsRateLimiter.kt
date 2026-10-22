package com.restpartijen.api.product.service

import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

// Open Food Facts allows 15 product reads per minute per IP and may block an IP that goes over (§10.3);
// 10 leaves room for a retry or a second instance (decision Eva, 22 Oct).
private const val MAX_CALLS = 10

// Half of the total, so one supplier, a loop in the app or a stolen token cannot use it up for everybody
// (security review M-2, decision Eva, 22 Oct).
private const val MAX_CALLS_PER_USER = 5

private val WINDOW = 1.minutes

/**
 * Counts the calls to Open Food Facts over the last minute: at most 10 for the application, 5 per supplier
 * (security review F-01, M-2). Above a limit the lookup gives manual entry instead of calling out.
 *
 * It logs a warning when it starts refusing, naming the supplier, and one line when it accepts again;
 * so the log shows who reached a limit without one line per refused call.
 *
 * @param clock decides when a call has left the window (GI-6)
 */
class OpenFoodFactsRateLimiter(private val clock: Clock) {
    private val log = LoggerFactory.getLogger(OpenFoodFactsRateLimiter::class.java)

    // Times of the allowed calls, oldest first: for the application and per supplier user id.
    private val allCalls = ArrayDeque<Instant>()
    private val callsPerUser = HashMap<Long, ArrayDeque<Instant>>()

    // Refused calls since the last allowed one, for the line that says the limit is open again.
    private var refusedInARow = 0

    /** True, and counted, when [userId] may call out now; false, and not counted, when a limit is reached. */
    fun tryAcquire(userId: Long): Boolean = synchronized(this) {
        val now = clock.now()
        allCalls.dropOlderThan(now)
        // Suppliers without calls in the window are forgotten, so the map stays as small as the active suppliers.
        callsPerUser.values.removeAll { it.dropOlderThan(now); it.isEmpty() }
        val userCalls = callsPerUser.getOrPut(userId) { ArrayDeque() }

        val limitReached = when {
            allCalls.size >= MAX_CALLS -> "the limit of $MAX_CALLS per minute"
            userCalls.size >= MAX_CALLS_PER_USER -> "the limit of $MAX_CALLS_PER_USER per minute per supplier"
            else -> null
        }
        if (limitReached != null) {
            if (refusedInARow == 0) {
                log.warn("Open Food Facts lookups refused: {} reached by user {}", limitReached, userId)
            }
            refusedInARow++
            false
        } else {
            if (refusedInARow > 0) log.info("Open Food Facts lookups accepted again, after {} refused", refusedInARow)
            refusedInARow = 0
            allCalls.addLast(now)
            userCalls.addLast(now)
            true
        }
    }

    private fun ArrayDeque<Instant>.dropOlderThan(now: Instant) {
        while (isNotEmpty() && now - first() >= WINDOW) removeFirst()
    }
}
