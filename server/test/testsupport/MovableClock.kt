package com.restpartijen.api.testsupport

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A clock the test can move forward, to pass a time limit without waiting.
 *
 * @property now the moment the clock returns; a test assigns a later moment to move time forward
 */
class MovableClock(var now: Instant) : Clock {
    override fun now() = now
}
