package com.restpartijen.api.shared
/**
 * An amount in whole euro cents (decision A).
 * A value class: a plain Long at runtime, but its own type in the code,
 * so a price can never be mixed up with an id or a quantity.
 */
@JvmInline
value class Money(val cents: Long) {
    init {
        require(cents >= 0) { "An amount cannot be negative" }
    }
}