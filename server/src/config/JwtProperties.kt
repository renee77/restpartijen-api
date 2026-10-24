package com.restpartijen.api.config

import io.ktor.server.config.ApplicationConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

data class JwtProperties(
    val secret: String,
    val issuer: String,
    val audience: String,
    val validity: Duration,
) {
    override fun toString(): String {
        return "JwtProperties(secret=***, issuer=$issuer, audience=$audience, validity=$validity)"
    }
}

private const val MIN_SECRET_BYTES = 32

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
        validity = validity
    )
}

/** Reads a jwt.* value that must be present. application.yaml is the only source of these values. */
private fun ApplicationConfig.requiredJwtValue(key: String): String =
    propertyOrNull("jwt.$key")?.getString()?.trim()?.ifBlank { null }
        ?: error("jwt.$key is not set. Add it to the jwt section of application.yaml.")
