package com.restpartijen.api.security.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import com.restpartijen.api.shared.Role
import java.util.Date
import kotlin.time.Clock
import kotlin.time.toJavaInstant

/**
 * Builds and verifies JWT tokens.
 *
 * The JWT library checks the expiry against the real system time and offers no
 * way to pass it our [Clock]. Expiry is therefore checked by [isExpired], which
 * uses the same injected clock as [createToken].
 */
class JwtConfig(
    private val settings: JwtSettings,
    private val clock: Clock,
) {
    // HMAC with SHA-256 and the secret from the settings.
    private val algorithm = Algorithm.HMAC256(settings.secret)

    // Used by the JWT provider in configureSecurity() to check signature, issuer and audience.
    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(settings.issuer)
        .withAudience(settings.audience)
        .ignoreIssuedAt()
        .build()
    // Only the user id and the role go into the token: no password, no e-mail.
    fun createToken(userId: Long, role: Role): String {
        val now = clock.now()
        return JWT.create()
            .withIssuer(settings.issuer)
            .withAudience(settings.audience)
            .withClaim(JwtClaims.USER_ID, userId)
            .withClaim(JwtClaims.ROLE, role.name)
            .withIssuedAt(now.toJavaInstant())
            .withExpiresAt((now + settings.validity).toJavaInstant())
            .sign(algorithm)
    }

/** True when the token has expired according to our injected [Clock]; a missing `exp` counts as expired. */
fun isExpired(expiresAt: Date?): Boolean =
    expiresAt == null || clock.now().toEpochMilliseconds() >= expiresAt.time
}