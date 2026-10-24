package com.restpartijen.api.testsupport

import kotlin.time.Clock
import kotlin.time.Instant

/** A clock that always returns [instant], so dates in a test are predictable. */
class FixedClock(private val instant: Instant) : Clock {
    override fun now() = instant
}
