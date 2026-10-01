package com.restpartijen.api.security

import com.restpartijen.api.security.service.JwtConfig
import com.restpartijen.api.security.service.PROVIDER_NAME
import com.restpartijen.api.security.service.configureSecurity
import com.restpartijen.api.shared.Role
import com.restpartijen.api.shared.UnauthorizedException
import com.restpartijen.api.shared.ForbiddenException
import com.restpartijen.api.testsupport.FixedClock
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class SecurityTest {
    private val jwtConfig = JwtConfig(TestJwtSettings.default, TestClock.fixed)


    // TEMPORARY TESTING SETUP: Configure status pages to handle exceptions and respond with appropriate HTTP status codes. This is used in the test application to simulate production error handling.
    private fun Application.configureStatusPages() {
        install(StatusPages) {
            exception<UnauthorizedException> { call, _ ->
                call.respond(HttpStatusCode.Unauthorized)
            }
            exception<Throwable> { call, cause ->
                cause.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError)
            }
            exception<ForbiddenException> { call, _ ->
                call.respond(HttpStatusCode.Forbidden)
            }
        }
    }

    // Test app: same error handling as production, security is installed, one protected route.
    private fun Application.setUpTestApp() {
        configureStatusPages()
        configureSecurity(jwtConfig)
        // One protected route for testing purposes. It requires authentication via the JWT provider.
        routing {
            authenticate(PROVIDER_NAME) {
                get("/test/protected") { call.respondText("ok") }
            }
        }
    }

    // Happy path
    @Test
    fun `valid token gets access`() = testApplication {
        // Arrange: set up the test application with security and a protected route
        application { setUpTestApp() }

        // Act: make a GET request to the protected route with a valid token
        val response = client.get("/test/protected") {
            bearerAuth(TestTokens().createTestToken(userId = 1L, role = Role.COLLECTOR, clock = Clock.System))
        }

        // Assert: the response status should be 200 OK, indicating successful access
        assertEquals(HttpStatusCode.OK, response.status)
    }

    // Sad path: no token
    @Test
    fun `request without token gets 401`() = testApplication {
        application { setUpTestApp() }

        val response = client.get("/test/protected")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    // Sad path: signed with another secret
    @Test
    fun `token with wrong secret gets 401`() = testApplication {
        application { setUpTestApp() }
        val otherSettings = TestJwtSettings.default.copy(secret = "some-other-secret")

        val response = client.get("/test/protected") {
            bearerAuth(TestTokens().createTestToken(userId = 1L, role = Role.COLLECTOR, settings = otherSettings))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    // Sad path: expired token
    @Test
    fun `expired token gets 401`() = testApplication {
        application { setUpTestApp() }
        val twoDaysAgo = FixedClock(Clock.System.now() - 2.days)

        val response = client.get("/test/protected") {
            bearerAuth(TestTokens().createTestToken(userId = 1L, role = Role.COLLECTOR, clock = twoDaysAgo))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

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
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}