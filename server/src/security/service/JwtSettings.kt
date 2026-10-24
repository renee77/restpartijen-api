package com.restpartijen.api.security.service

import kotlin.time.Duration

/**
 * JWT settings. Within production, it is filled with values from config, in tests it is filled with fixed test values.
 *
 * @property secret the secret that signs and verifies every token
 * @property issuer the issuer that every token names and every check expects
 * @property audience the audience that every token names and every check expects
 * @property validity how long a token stays valid after it is issued
 */
data class JwtSettings(val secret: String, val issuer: String, val audience: String, val validity: Duration) {
    /** Never shows the secret, so it cannot end up in a log by accident (§16.7). */
    override fun toString(): String = "JwtSettings(secret=***, issuer=$issuer, audience=$audience, validity=$validity)"
}
