package com.restpartijen.api.config

import io.ktor.server.config.ApplicationConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * The settings of the `jwt` section in application.yaml.
 *
 * This package only reads and checks them; Application.module() copies them into the JwtSettings that security uses.
 *
 * @property secret the signing secret from JWT_SECRET, at least 32 bytes.
 * @property issuer the issuer that every token names and every check expects.
 * @property audience the audience that every token names and every check expects.
 * @property validity how long a token stays valid after it is issued.
 */
data class JwtProperties(val secret: String, val issuer: String, val audience: String, val validity: Duration) {
    /** Shows the secret as `***`, so logging these properties never leaks it. */
    override fun toString(): String =
        "JwtProperties(secret=***, issuer=$issuer, audience=$audience, validity=$validity)"
}

/** The shortest accepted secret in bytes: 256 bits, the key size of the HMAC-SHA256 signature. */
private const val MIN_SECRET_BYTES = 32

/**
 * Reads the `jwt` section and checks every value.
 *
 * A missing, blank or too short secret, a missing or blank issuer, audience or validityHours, or a validity
 * that is not a whole number of hours above 0 stops the application with a message that says what to fix.
 * No message contains the secret.
 */
fun ApplicationConfig.jwtProperties(): JwtProperties {
    val jwtSecret = propertyOrNull("jwt.secret")?.getString()?.trim()?.ifBlank { null }

    val secret = when {
        jwtSecret == null ->
            error("JWT_SECRET is not set. Set it before starting the application; see README, section Configuration.")

        jwtSecret.toByteArray().size < MIN_SECRET_BYTES ->
            error("JWT_SECRET is too short: use at least $MIN_SECRET_BYTES bytes, for example: openssl rand -base64 32")

        else -> jwtSecret
    }

    val issuer = requiredJwtValue("issuer")
    val audience = requiredJwtValue("audience")

    val rawValidity = requiredJwtValue("validityHours")
    val validityHours = rawValidity.toLongOrNull()?.takeIf { it > 0 }
        ?: error("Invalid jwt.validityHours '$rawValidity': use a whole number of hours greater than 0.")
    val validity = validityHours.hours

    return JwtProperties(
        secret = secret,
        issuer = issuer,
        audience = audience,
        validity = validity,
    )
}

/** Reads a jwt.* value that must be present. application.yaml is the only source of these values. */
private fun ApplicationConfig.requiredJwtValue(key: String): String =
    propertyOrNull("jwt.$key")?.getString()?.trim()?.ifBlank { null }
        ?: error("jwt.$key is not set. Add it to the jwt section of application.yaml.")
