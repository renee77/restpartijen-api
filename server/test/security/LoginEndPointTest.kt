package com.restpartijen.api.security

import com.restpartijen.api.plugins.ErrorCode
import com.restpartijen.api.plugins.ErrorResponse
import com.restpartijen.api.security.dto.LoginResponse
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.security.service.JwtConfig
import com.restpartijen.api.security.service.configureSecurity
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import com.restpartijen.api.testsupport.setUpAuthTestApp
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import com.restpartijen.api.security.service.PROVIDER_NAME

class LoginEndpointTest {
    // Arrange: use a fixed clock for predictable token timestamps
    val testClock = TestClock.fixedNow()
    private suspend fun HttpResponse.errorBody(): ErrorResponse =
        Json.decodeFromString<ErrorResponse>(bodyAsText())

    // Testbasics
    private val email = "login@mail.com"
    private val password = "MyTestPassword123"
    private val jwtConfig = JwtConfig(TestJwtSettings.default, testClock)

    // Setting up the TestApp and the routing, for this test
    private fun ApplicationTestBuilder.setUpLoginApp() {
        application {
            setUpAuthTestApp(jwtConfig)
            // Real security plugin + one protected route, to prove the issued token is accepted.
            configureSecurity(jwtConfig)
            routing {
                authenticate(PROVIDER_NAME) {
                    get("/test/protected") { call.respondText("ok") }
                }
            }
        }
    }

    private suspend fun HttpClient.registerDefaultUser() =
        post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$email","password":"$password","displayName":"TestName"}""")
        }

    private suspend fun HttpClient.login(json: String): HttpResponse =
        post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(json)
        }

    private fun loginBody(email: String = this.email, password: String = this.password) =
        """{"email":"$email","password":"$password"}"""

    @Test
    fun `login returns 200 with token and role`() = testApplication {
        // Arrange
        createEmptyTestDatabase(UsersTable)
        setUpLoginApp()
        client.registerDefaultUser()

        // Act
        val response = client.login(loginBody())

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.decodeFromString<LoginResponse>(response.bodyAsText())
        assertEquals(Role.COLLECTOR, body.role)
        assertTrue(body.token.isNotBlank())
    }

    @Test
    fun `login returns no password and no hash`() = testApplication {
        // Arrange
        createEmptyTestDatabase(UsersTable)
        setUpLoginApp()
        client.registerDefaultUser()

        // Act
        val text = client.login(loginBody()).bodyAsText()

        // Assert
        assertFalse(text.contains(password))
        assertFalse(text.contains("passwordHash", ignoreCase = true))
        assertFalse(text.contains("argon2"))
    }

    @Test
    fun `login with other email casing returns 200`() = testApplication {
        // Arrange
        createEmptyTestDatabase(UsersTable)
        setUpLoginApp()
        client.registerDefaultUser()

        // Act
        val response = client.login(loginBody(email = "LOGIN@Mail.com"))

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `wrong password returns 401 with code UNAUTHORIZED`() = testApplication {
        // Arrange
        createEmptyTestDatabase(UsersTable)
        setUpLoginApp()
        client.registerDefaultUser()

        // Act
        val response = client.login(loginBody(password = "WrongPassword123"))

        // Assert
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorCode.UNAUTHORIZED, response.errorBody().code)
    }

    @Test
    fun `unknown email returns 401 with the same message as a wrong password`() = testApplication {
        // Arrange
        createEmptyTestDatabase(UsersTable)
        setUpLoginApp()
        client.registerDefaultUser()

        // Act
        val wrongPassword = client.login(loginBody(password = "WrongPassword123"))
        val unknownEmail = client.login(loginBody(email = "other@mail.com"))

        // Assert
        assertEquals(HttpStatusCode.Unauthorized, unknownEmail.status)
        assertEquals(wrongPassword.errorBody().message, unknownEmail.errorBody().message)
    }

    @Test
    fun `login with a missing field returns 400`() = testApplication {
        // Arrange
        createEmptyTestDatabase(UsersTable)
        setUpLoginApp()

        // Act
        val response = client.login("""{"email":"$email"}""")

        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(ErrorCode.VALIDATION_ERROR, response.errorBody().code)
    }

    @Test
    fun `the token from login works on a protected route`() = testApplication {
        // Arrange
        createEmptyTestDatabase(UsersTable)
        setUpLoginApp()
        client.registerDefaultUser()
        val token = Json.decodeFromString<LoginResponse>(client.login(loginBody()).bodyAsText()).token

        // Act
        val response = client.get("/test/protected") { bearerAuth(token) }

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `a protected route without a token returns 401`() = testApplication {
        // Arrange
        createEmptyTestDatabase(UsersTable)
        setUpLoginApp()

        // Act
        val response = client.get("/test/protected")

        // Assert
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `login of a collector shows supplierId as null`() = testApplication {
        // Arrange
        createEmptyTestDatabase(UsersTable)
        setUpLoginApp()
        client.registerDefaultUser()

        // Act
        val text = client.login(loginBody()).bodyAsText()

        // Assert: the field is there and null. The app reads null as "no supplier".
        assertEquals(null, Json.decodeFromString<LoginResponse>(text).supplierId)
        assertTrue(text.contains("\"supplierId\":null"))
    }
}