package com.restpartijen.api.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.restpartijen.api.security.service.JWTSettings
import com.restpartijen.api.shared.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import com.restpartijen.api.testsupport.FixedClock
import kotlin.time.Instant
import kotlin.time.Duration

class TestTokensTest {
    // Getting the test settings from the TestJwtSettings, which provides default values for testing purposes.
    private val testSettings = TestJwtSettings.default

    // Verifies the token the way the real application will, using  the same settings and algorithm.
    private fun verifyToken(token: String, secret: String = testSettings.secret) {
        // Create an algorithm instance using the HMAC256 algorithm with the provided secret.
        val algorithm = Algorithm.HMAC256(secret)
        // Create a JWT verifier with the algorithm, issuer, and audience from the test settings.
        val verifier = JWT.require(algorithm)
            .withIssuer(testSettings.issuer)
            .withAudience(testSettings.audience)
            .build()

        // Verify the token using the verifier. If the token is invalid or expired, this will throw an exception.
        verifier.verify(token)
    }

    // Happy path, token is valid and uses verification on role and userId. No exception is thrown.
    @Test
    fun `token carries the userId and role and passes verificiation` () {
        // Arrange. Nothing needs to be arranged, it uses the predefined defaults and clock.
        // Act. Creating and a test token
        val token = TestTokens().createTestToken(
            userId = 1L,
            role = Role.COLLECTOR,
        )
        val decoded = JWT.decode(token)
        // Assert. Verify that the decoded token contains the expected userId and role.
        // Subject is the default location of an id within a JWT token, and the role is stored as a claim.
        assertEquals("1", decoded.subject)
        assertEquals(Role.COLLECTOR.name, decoded.getClaim("role").asString())
    }


    // Happy path, token is valid for 24 hours and no exception is thrown.
    @Test
    fun `token is valid for 24 hours`() {
        // Arrange. Create a fixed clock to ensure the token is generated at a known time.
        val testClock = FixedClock(Instant.parse("2026-09-26T12:00:00Z"))
        // Act. Create a testtoken with all required information, and the testsettings
        val token = TestTokens().createTestToken(
            userId = 1L,
            role = Role.COLLECTOR,
            settings = testSettings,
            clock = testClock
        )
        val decoded = JWT.decode(token)

        val issuedAt = Instant.fromEpochMilliseconds(decoded.issuedAt.time)
        val expiresAt = Instant.fromEpochMilliseconds(decoded.expiresAt.time)

        val validity = expiresAt - issuedAt

        // Assert
        assertEquals(Duration.parse("24h"), validity)
    }

    // Sad path. The token is signed with another secret, so it should fail verification and throw a SignatureVerificationException.
    @Test
    fun `token signed with another secret fails` () {
        // Arange. Create a test token with the default settings and clock.
        val token = TestTokens().createTestToken(
            userId = 1L,
            role = Role.COLLECTOR
        )
        // Act. We define the other secret.
        val otherSecret = "other-secret"

        // Assert.We expect it will throw a SignatureVerificationException, because the token was signed with a different secret.
        assertFailsWith<SignatureVerificationException> { verifyToken(token, otherSecret) }
    }

    // Sad path. The token is expired, so it should fail verification and throw a TokenExpiredException.
    @Test
    fun `token is expired and rejected` () {
        // Arrange. Create the clock with a time in the past, so the token will be expired when we verify it.
        val pastClock = FixedClock(Instant.parse("2026-09-25T12:00:00Z")) // 24 hours in the past
        val testToken = TestTokens().createTestToken(
            userId = 1L,
            role = Role.COLLECTOR,
            settings = testSettings,
            clock = pastClock
        )
        // Act and Assert. We expect it will throw a TokenExpiredException, because the token was created with a past expiration time.
        assertFailsWith<TokenExpiredException> { verifyToken(testToken, testSettings.secret) }
    }
}