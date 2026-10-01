package com.restpartijen.api.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.restpartijen.api.shared.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import com.restpartijen.api.testsupport.FixedClock
import kotlin.time.Instant
import kotlin.time.Duration

class RoleAuthTests {
    // Getting the test settings from the TestJwtSettings, which provides default values for testing purposes.
    private val testSettings = TestJwtSettings.default

    // Seeing as the tokens get verified the same way, we can use the same verification function as in TestTokensTest.
    private fun verifyToken(token: String, secret: String = testSettings.secret) {
        val algorithm = Algorithm.HMAC256(secret)
        val verifier = JWT.require(algorithm)
            .withIssuer(testSettings.issuer)
            .withAudience(testSettings.audience)
            .build()

        verifier.verify(token)
    }

    // Happy path -> Admin get access to the admin only route.
    // Act: Create a test token with the admin role and a fixed clock to ensure the token is generated at a known time.
    val token = TestTokens().createTestToken(
        userId = 1L,
        role = Role.ADMIN
    )
    val decoded = JWT.decode(token)
    // Assert. Verify that the decoded token contains the expected userId and role.

}