package com.restpartijen.api.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.restpartijen.api.security.service.JwtSettings
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.FixedClock
import java.util.Date
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Default JWT settings for tests, so tokens can be created without providing settings every time.
 */
object TestJwtSettings {
    val default = JwtSettings(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience"
    )
}

/** One fixed moment, shared by the token helper and the JwtConfig in tests. */
object TestClock {
    val fixed = FixedClock(Instant.parse("2026-09-26T12:00:00Z"))
}

/**
 * Helper class to create test tokens. It uses the default JWT settings and a fixed clock,
 * so token generation is consistent.
 */
class TestTokens {

    /** Creates a valid test token for the given user and role. */
    fun createTestToken(
        userId: Long,
        role: Role,
        settings: JwtSettings = TestJwtSettings.default,
        clock: Clock = TestClock.fixed
    ): String = createTestTokenWithRoleName(userId, role.name, settings, clock)

    /**
     * Creates a token with any text as role, for testing roles that are not in Role.
     * Everything else is identical to a valid token.
     */
    fun createTestTokenWithRoleName(
        userId: Long,
        roleName: String,
        settings: JwtSettings = TestJwtSettings.default,
        clock: Clock = TestClock.fixed
    ): String {
        val now = clock.now()

        return JWT.create()
            .withIssuer(settings.issuer)
            .withAudience(settings.audience)
            .withClaim("userId", userId)
            .withClaim("role", roleName)
            .withIssuedAt(Date(now.toEpochMilliseconds()))
            .withExpiresAt(Date((now + settings.validity).toEpochMilliseconds()))
            // Sign the token with the HMAC256 algorithm using the secret from the settings.
            .sign(Algorithm.HMAC256(settings.secret))
    }
}