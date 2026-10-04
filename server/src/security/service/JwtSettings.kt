package com.restpartijen.api.security.service

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * JWT settings. Within production, it is filled with values from config, in tests it is filled with fixed test values
*/
data class JwtSettings(
    val secret: String,
    val issuer: String,
    val audience: String,
    val validity: Duration = 24.hours
) {
// Never show the secret, so it cannot end up in a log by accident (§16.7).
override fun toString(): String =
    "JwtSettings(secret=***, issuer=$issuer, audience=$audience, validity=$validity)"
}