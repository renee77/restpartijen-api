package com.restpartijen.api.security

import com.restpartijen.api.plugins.ErrorResponse
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.FixedClock
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours

/** Token validation on a protected route: a valid token passes; no token, another secret or expiry gives 401. */
class ValidationTests {
    // Happy path
    @Test
    fun `valid token gets access`() = testApplication {
        // Arrange: set up the test application with security and a protected route
        application { setUpTestApp() }

        // Act: make a GET request to the protected route with a valid token
        val response = client.get("/test/protected") {
            bearerAuth(TestTokens().createTestToken(userId = 1L, role = Role.COLLECTOR))
        }

        // Assert: the response status should be 200 OK, indicating successful access
        assertEquals(HttpStatusCode.OK, response.status)
    }

    // Sad path: no token
    @Test
    fun `request without token gets 401`() = testApplication {
        application { setUpTestApp() }

        val response = client.get("/test/protected")

        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("Authentication required", error.message)
    }

    // Sad path: signed with another secret
    @Test
    fun `token with wrong secret gets 401`() = testApplication {
        application { setUpTestApp() }
        val otherSettings = TestJwtSettings.default.copy(secret = "some-other-secret")

        val response = client.get("/test/protected") {
            bearerAuth(TestTokens().createTestToken(userId = 1L, role = Role.COLLECTOR, settings = otherSettings))
        }

        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("Authentication required", error.message)
    }

    /** Stands still from the real "now", because the verifier checks expiry against the real time. */
    private val testClock = TestClock.fixedNow()

    // Sad path: expired token
    @Test
    fun `expired token gets 401`() = testApplication {
        // Arrange: a token that was issued 48 hours ago, so it expired 24 hours ago
        application { setUpTestApp() }
        val twoDaysAgo = FixedClock(testClock.now() - 48.hours)

        // Act
        val response = client.get("/test/protected") {
            bearerAuth(TestTokens().createTestToken(userId = 1L, role = Role.COLLECTOR, clock = twoDaysAgo))
        }

        // Assert: the status, and the message that says why
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("Authentication required", error.message)
    }
}
