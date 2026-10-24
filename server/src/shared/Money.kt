package com.restpartijen.api.shared

import kotlin.math.roundToLong
/**
 * An amount in whole euro cents (decision ADR-07, B-13).
 * Create it only through fromCents or fromEuros.
 */
@JvmInline
value class Money private constructor(val cents: Long) {
    init {
        require(cents >= 0) { "An amount cannot be negative" }
    }

    /** Turning the given double into cents */
    fun toEuros(): Double = cents / 100.0

    /**
     * Factories for [Money]. The constructor is private on purpose, so these
     * two functions are the only ways to create an amount. That keeps the
     * euro-to-cents conversion in one place (B-25).
     */
    companion object {
        /** Use when the value is already in cents, for example from the database. */
        fun fromCents(cents: Long): Money = Money(cents)

        /** Use for euros from outside (request, query parameter). Rounds to the nearest cent. */
        fun fromEuros(euros: Double): Money {
            require(euros.isFinite()) { "The number must be finite" }
            return Money((euros * 100).roundToLong())
        }
    }
}
