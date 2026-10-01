package com.restpartijen.api.security

import com.auth0.jwt.JWT
import com.restpartijen.api.security.service.JwtConfig
import com.restpartijen.api.shared.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

class JwtConfigTest {
    @Test
    fun `token only contains userId and role`() {
        // Arrange the configuration with default test settings and a system clock
        val jwtConfig = JwtConfig(TestJwtSettings.default, Clock.System)

        // Act, create a token and decode it to inspect the claims
        val decoded = JWT.decode(jwtConfig.createToken(userId = 1L, role = Role.COLLECTOR))

        // Assert: standard claims (iss, aud, iat, exp) excluded, only our own are left
        val ownClaims = decoded.claims.keys - setOf("iss", "aud", "iat", "exp")
        assertEquals(setOf("userId", "role"), ownClaims)
        assertEquals(1L, decoded.getClaim("userId").asLong())
        assertEquals("COLLECTOR", decoded.getClaim("role").asString())
    }
}