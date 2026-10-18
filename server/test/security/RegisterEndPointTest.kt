package com.restpartijen.api.security

import com.restpartijen.api.plugins.ErrorCode
import com.restpartijen.api.plugins.ErrorResponse
import com.restpartijen.api.testsupport.setUpAuthTestApp
import com.restpartijen.api.security.dto.UserResponse
import com.restpartijen.api.security.repository.ExposedUserRepository
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.security.service.JwtConfig
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class RegisterEndpointTest {

    private suspend fun HttpResponse.errorBody(): ErrorResponse =
        Json.decodeFromString<ErrorResponse>(bodyAsText())
    // Set up the test app: same plugins as production, real service on an empty H2 database.
    private val jwtConfig = JwtConfig(TestJwtSettings.default, TestClock.fixed)

    private fun ApplicationTestBuilder.setUpRegisterApp() {
        application { setUpAuthTestApp(jwtConfig) }
    }

    private suspend fun HttpClient.register(json: String): HttpResponse =
        post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(json)
        }

    private fun body(
        email: String = "test@mail.com",
        password: String = "MyTestPassword123",
        displayName: String = "TestName",
        extra: String = "",
    ) = """{"email":"$email","password":"$password","displayName":"$displayName"$extra}"""

    private val fixedNow = Instant.parse("2026-01-15T10:30:00Z")

    @Test
    fun `register returns 201 with a COLLECTOR and no secrets`() = testApplication {
        createEmptyTestDatabase(UsersTable)
        setUpRegisterApp()

        val response = client.register(body())
        val text = response.bodyAsText()

        assertEquals(HttpStatusCode.Created, response.status)
        val user = Json.decodeFromString<UserResponse>(text)
        assertEquals(Role.COLLECTOR, user.role)
        assertEquals("test@mail.com", user.email)
        // The body must not leak the password or the hash
        assertFalse(text.contains("MyTestPassword123"))
        assertFalse(text.contains("passwordHash", ignoreCase = true))
        assertFalse(text.contains("argon2"))
    }

    @Test
    fun `register refuses an unknown role field and creates no account`() = testApplication {
        // Arrange: an empty database and the register app
        createEmptyTestDatabase(UsersTable)
        setUpRegisterApp()

        // Act: a body that tries to choose its own role
        val response = client.register(body(extra = ""","role":"ADMIN""""))

        // Assert: the request is refused as a structural error
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(ErrorCode.VALIDATION_ERROR, response.errorBody().code)

        // Assert: no account was created, so no ADMIN can exist
        assertNull(ExposedUserRepository().findByEmail("test@mail.com"))
    }

    @Test
    fun `register with an existing email returns 409, also in other casing`() = testApplication {
        createEmptyTestDatabase(UsersTable)
        setUpRegisterApp()
        client.register(body(email = "test@mail.com"))

        val response = client.register(body(email = "TEST@Mail.com"))

        assertEquals(HttpStatusCode.Conflict, response.status)
        assertEquals(ErrorCode.CONFLICT, response.errorBody().code)
    }

    @Test
    fun `register with a too short password returns 422 and field password`() = testApplication {
        createEmptyTestDatabase(UsersTable)
        setUpRegisterApp()

        val response = client.register(body(password = "abc"))
        val error = response.errorBody()

        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(ErrorCode.DOMAIN_RULE_VIOLATION, error.code)
        assertEquals("password", error.field)
    }

    @Test
    fun `register with an invalid email returns 422 and field email`() = testApplication {
        createEmptyTestDatabase(UsersTable)
        setUpRegisterApp()

        val response = client.register(body(email = "not-an-email"))
        val error = response.errorBody()

        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals("email", error.field)
    }

    @Test
    fun `register with a blank display name returns 422 and field displayName`() = testApplication {
        createEmptyTestDatabase(UsersTable)
        setUpRegisterApp()

        val response = client.register(body(displayName = "   "))
        val error = response.errorBody()

        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals("displayName", error.field)
    }

    @Test
    fun `register with a missing field returns 400`() = testApplication {
        createEmptyTestDatabase(UsersTable)
        setUpRegisterApp()

        val response = client.register("""{"email":"test@mail.com","displayName":"TestName"}""")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(ErrorCode.VALIDATION_ERROR, response.errorBody().code)
    }

    @Test
    fun `register with broken JSON returns 400`() = testApplication {
        createEmptyTestDatabase(UsersTable)
        setUpRegisterApp()

        val response = client.register("{ this is not json")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `the stored password is an Argon2id hash`() = testApplication {
        createEmptyTestDatabase(UsersTable)
        setUpRegisterApp()
        client.register(body())

        val stored = ExposedUserRepository().findByEmail("test@mail.com")

        assertNotNull(stored)
        assertTrue(stored.passwordHash.startsWith("\$argon2id\$"))
    }
}