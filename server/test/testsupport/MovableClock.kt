package com.restpartijen.api.testsupport

import kotlin.time.Clock
import kotlin.time.Instant

/** A clock the test can move forward, to pass a time limit without waiting. */
class MovableClock(var now: Instant) : Clock {
    override fun now() = now
}
