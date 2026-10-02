package com.restpartijen.api.config

import io.ktor.server.config.ApplicationConfig
import java.security.SecureRandom
import kotlin.io.encoding.Base64
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

data class JwtProperties(
    val secret: String,
    val issuer: String,
    val audience: String,
    val validity: Duration,
//    val secretGenerated: Boolean
) {
    override fun toString(): String {
        return "JwtProperties(secret=***, issuer=$issuer, audience=$audience, validity=$validity)"
    }
                //", secretGenerated=$secretGenerated)"
}

private const val MIN_SECRET_BYTES = 32
//values are the same as in application.yaml, required here for testing
private const val DEFAULT_ISSUER = "restpartijen-api"
private const val DEFAULT_AUDIENCE = "restpartijen-app"
private const val DEFAULT_VALIDITY_HOURS = 24L

fun ApplicationConfig.jwtProperties(): JwtProperties {
    val jwtSecret = propertyOrNull("jwt.secret")?.getString()?.trim()?.ifBlank { null }

    val secret = when {
        jwtSecret == null -> fallbackSecret()
        jwtSecret.toByteArray().size < MIN_SECRET_BYTES ->
            error("JWT_SECRET is too short: use at least $MIN_SECRET_BYTES bytes, for example: openssl rand -base64 32")
        else -> jwtSecret
    }

    val issuer = propertyOrNull("jwt.issuer")?.getString() ?: DEFAULT_ISSUER
    val audience = propertyOrNull("jwt.audience")?.getString() ?: DEFAULT_AUDIENCE

    val rawValidity = propertyOrNull("jwt.validityHours")?.getString()
    val validityHours = if (rawValidity == null) {
        DEFAULT_VALIDITY_HOURS
    } else {
        rawValidity.trim().toLongOrNull()?.takeIf { it > 0 }
            ?: error("Invalid jwt.validityHours '$rawValidity': use a whole number of hours greater than 0.")
    }
    val validity = validityHours.hours

    return JwtProperties(
        secret = secret,
        issuer = issuer,
        audience = audience,
        validity = validity
    )
        //, secretGenerated = secretGenerated

}
//Keuze A voor nu: ask Paul for input
//private fun fallbackSecret(): String {
//    val bytes = ByteArray(MIN_SECRET_BYTES).also { SecureRandom().nextBytes(it) }
//    return Base64.encode(bytes)
//}

private fun fallbackSecret(): Nothing =
    error("JWT_SECRET is not set. Set it before starting the application; see README, section Configuration.")