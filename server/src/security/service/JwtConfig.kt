package com.restpartijen.api.security.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.restpartijen.api.shared.Role
import kotlin.time.Clock
import kotlin.time.toJavaInstant


/**
 * Builds and verifies JWT tokens.
 * */
class JwtConfig(
    private val settings: JwtSettings,
    private val clock: Clock,
) {
    // The algorithm used to sign the JWT tokens. It uses HMAC with SHA-256 and the secret from the settings.
    private val algorithm = Algorithm.HMAC256(settings.secret)

    // Used by the JWT provider in configureSecurity() to check incoming tokens.
    val verifier = JWT.require(algorithm)
        .withIssuer(settings.issuer)
        .withAudience(settings.audience)
        .build()

    // Only the user id and the role go into the token: no password, no e-mail.
    fun createToken(userId: Long, role: Role): String {
        val now = clock.now()
        return JWT.create()
            .withIssuer(settings.issuer)
            .withAudience(settings.audience)
            .withClaim("userId", userId)
            .withClaim("role", role.name)
            .withIssuedAt(now.toJavaInstant())
            .withExpiresAt((now + settings.validity).toJavaInstant())
            .sign(algorithm)
    }
}