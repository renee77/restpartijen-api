package com.restpartijen.api.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.restpartijen.api.security.service.JWTSettings
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.FixedClock
import kotlin.time.Clock
import kotlin.time.Instant
import java.util.Date

/** Creating an object with default JWT settings for testing purposes, so I can test tokens without having to provide settings every time.
*/
object TestJwtSettings {
    val default = JWTSettings(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience"
    )
}

/** A helper class to create test tokens for testing purposes. It uses the default JWT settings and a fixed clock to ensure consistent token generation.
 */
class TestTokens {
    private val defaultClock = FixedClock(Instant.parse("2026-09-26T12:00:00Z"))
    fun createTestToken(
        userId: Long,
        role: Role,
        settings: JWTSettings = TestJwtSettings.default,
        clock: Clock = defaultClock,
    ): String {
        val now = clock.now()

        /** Create a JWT token with the provided userId, role, and settings. The token will have an issuedAt and expiresAt claim based on the current time from the clock.
         */
        return JWT.create()
            .withIssuer(settings.issuer)
            .withAudience(settings.audience)
            .withSubject(userId.toString())
            .withClaim("role", role.name)
            .withIssuedAt(Date(now.toEpochMilliseconds()))
            .withExpiresAt(Date((now + settings.validity).toEpochMilliseconds()))
            // Sign the token with the HMAC256 algorithm using the secret from the settings.
            .sign(Algorithm.HMAC256(settings.secret))
    }
}