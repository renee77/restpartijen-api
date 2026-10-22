package com.restpartijen.api.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.restpartijen.api.security.service.JwtClaims
import com.restpartijen.api.security.service.JwtSettings
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.FixedClock
import java.util.Date
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.Duration.Companion.hours

/**
 * Default JWT settings for tests, so tokens can be created without providing settings every time.
 */
object TestJwtSettings {
    val default = JwtSettings(
        secret = "test-secret-that-is-long-enough-to-work-in-production",
        issuer = "test-issuer",
        audience = "test-audience",
        validity = 24.hours
    )
}

/** One fixed moment, shared by the token helper and the JwtConfig in tests. */
object TestClock {
    /** One fixed moment, for tests that do not go through the JWT verifier (for example ExpirySchedulerTest). */
    val fixed = FixedClock(Instant.parse("2026-09-26T12:00:00Z"))

    /**
     * For tests whose tokens go through the JWT verifier. It stands still during the test
     * (GI-6, decision 6.5), but starts at the real "now", because the verifier checks expiry
     * against the real time (#62).
     */
    fun fixedNow(): FixedClock = FixedClock(Clock.System.now())
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
        clock: Clock = TestClock.fixedNow()
    ): String = createTestTokenWithRoleName(userId, role.name, settings, clock)

    /**
     * Creates a token with any text as role, for testing roles that are not in Role.
     * Everything else is identical to a valid token.
     */
    fun createTestTokenWithRoleName(
        userId: Long,
        roleName: String,
        settings: JwtSettings = TestJwtSettings.default,
        clock: Clock = TestClock.fixedNow()
    ): String {
        val now = clock.now()

        return JWT.create()
            .withIssuer(settings.issuer)
            .withAudience(settings.audience)
            .withClaim(JwtClaims.USER_ID, userId)
            .withClaim(JwtClaims.ROLE, roleName)
            .withIssuedAt(Date(now.toEpochMilliseconds()))
            .withExpiresAt(Date((now + settings.validity).toEpochMilliseconds()))
            // Sign the token with the HMAC256 algorithm using the secret from the settings.
            .sign(Algorithm.HMAC256(settings.secret))
    }
}