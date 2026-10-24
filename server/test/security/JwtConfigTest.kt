package com.restpartijen.api.security

import com.auth0.jwt.JWT
import com.restpartijen.api.security.service.JwtConfig
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/** JwtConfig.createToken: the token carries the user id and the role, and nothing else of our own. */
class JwtConfigTest {
    @Test
    fun `token only contains userId and role`() {
        // Arrange: use a fixed clock for predictable token timestamps
        val testClock = FixedClock(
            Instant.parse("2026-10-20T12:00:00Z"),
        )
        // Arrange the configuration with default test settings and a test clock
        val jwtConfig = JwtConfig(TestJwtSettings.default, testClock)

        // Act, create a token and decode it to inspect the claims
        val decoded = JWT.decode(jwtConfig.createToken(userId = 1L, role = Role.COLLECTOR))

        // Assert: standard claims (iss, aud, iat, exp) excluded, only our own are left
        val ownClaims = decoded.claims.keys - setOf("iss", "aud", "iat", "exp")
        assertEquals(setOf("userId", "role"), ownClaims)
        assertEquals(1L, decoded.getClaim("userId").asLong())
        assertEquals("COLLECTOR", decoded.getClaim("role").asString())
    }
}
