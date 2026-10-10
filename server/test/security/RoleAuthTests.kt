package com.restpartijen.api.security

import com.restpartijen.api.plugins.ErrorResponse
import com.restpartijen.api.shared.Role
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

class RoleAuthTests {
    // Test with a token that has a role not defined in the Role enum. This should also result in a 401 Unauthorized response.
    @Test
    fun `token with invalid role gets 401`() = testApplication {
        // Arrange: set up the test application with security and a protected route
        application { setUpTestApp() }

        val token = TestTokens().createTestTokenWithRoleName(userId = 1L, roleName = "TESTER", clock = Clock.System)

        // Act: create a token with a role that is not in the Role enum and make a GET request to the protected route
        val response = client.get("/test/protected") {
            bearerAuth(token)
        }

        // Assert: the response status should be 401 Unauthorized, indicating that the token's role is not valid
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("Authentication required", error.message)
    }


    // Happy path -> Someone with a valid role for the route get access (in this case, a collector).
    @Test
    fun `token with valid role for route gets access`() = testApplication {
        // Arrange: Set up the test application with security and a role-protected route
        application { setUpTestApp() }

        val token = TestTokens().createTestToken(
            userId = 1L,
            role = Role.COLLECTOR,
            clock = Clock.System
        )

        // Act: Make a GET request to the role-protected route with a valid token that has the required role
        val response = client.get("/test/role-protected-collector") {
            bearerAuth(token)
        }

        // Assert. Verify that the role is valid and the access is granted.
        assertEquals(HttpStatusCode.OK, response.status)
    }

    // Sad path -> User with a role that is not allowed for the route should get a 403 Forbidden response.
    @Test
    fun `token with invalid role for route gets a 403`() = testApplication {
        // Arrange: Set up the test application with security and a role-protected route
        application { setUpTestApp() }

        val token = TestTokens().createTestToken(
            userId = 1L,
            role = Role.ADMIN,
            clock = Clock.System
        )

        // Act: Make a GET request to the role-protected route with a valid token that has the required role
        val response = client.get("/test/role-protected-collector") {
            bearerAuth(token)
        }

        // Assert. Verify that the decoded token contains the expected userId and role.
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText()).message
        assertEquals("Insufficient role", error)
    }


    // Happy path -> Current user information can be retrieved from a valid token.
    @Test
    fun `current user information can be retrieved from a valid token`() = testApplication {
        // Arrange: Set up the test application with security and a route to retrieve user information
        application { setUpTestApp() }

        val token = TestTokens().createTestToken(
            userId = 42L,
            role = Role.SUPPLIER,
            clock = Clock.System
        )

        // Act: Make a GET request to the user information route with a valid token
        val response = client.get("/test/current-user") {
            bearerAuth(token)
        }

        // Retrieve the response body as text to verify the user information
        val responseBody = response.bodyAsText()

        // Assert. Verify that the response contains the expected user information.
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("ok, role: SUPPLIER and id: 42", responseBody)
    }
}