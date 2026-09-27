package com.restpartijen.api.shared

import kotlin.time.Instant

/** Period in which a reserved product can be collected (§9.9). */
data class PickupWindow(val from: Instant, val until: Instant) {
    init {
        require(from < until) { "A pickup window must start before it ends" }
    }
}