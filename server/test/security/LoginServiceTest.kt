package com.restpartijen.api.security

import com.auth0.jwt.JWT
import com.restpartijen.api.security.model.User
import com.restpartijen.api.security.service.JwtClaims
import com.restpartijen.api.security.service.JwtConfig
import com.restpartijen.api.security.service.LoginService
import com.restpartijen.api.security.service.PasswordHasher
import com.restpartijen.api.shared.Role
import com.restpartijen.api.shared.UnauthorizedException
import com.restpartijen.api.testsupport.FakeUserRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Instant

class LoginServiceTest {

    private val email = "login@mail.com"
    private val password = "MyTestPassword123"

    private val repository = FakeUserRepository()
    private val passwordHasher = PasswordHasher()
    private val service = LoginService(
        userRepository = repository,
        passwordHasher = passwordHasher,
        jwtConfig = JwtConfig(TestJwtSettings.default, TestClock.fixed),
    )

    // Argon2id is slow, so hash once and reuse the hash in every test.
    private val storedHash = passwordHasher.hashPassword(password)

    private suspend fun saveUser(): User =
        repository.create(
            User(
                id = 0,
                email = email,
                passwordHash = storedHash,
                display_name = "TestName",
                role = Role.COLLECTOR,
                createdAt = Instant.parse("2026-01-15T10:30:00Z"),
            )
        )

    @Test
    fun `valid credentials return a token with the user's role`() = runTest {
        saveUser()

        val response = service.login(email, password)

        assertEquals(Role.COLLECTOR, response.role)
        assertTrue(response.token.isNotBlank())
    }

    @Test
    fun `the token contains the user id and role claims`() = runTest {
        val saved = saveUser()

        val token = JWT.decode(service.login(email, password).token)

        assertEquals(Role.COLLECTOR.name, token.getClaim(JwtClaims.ROLE).asString())
        assertEquals(saved.id, token.getClaim(JwtClaims.USER_ID).asLong())
    }

    @Test
    fun `wrong password is rejected with UnauthorizedException`() = runTest {
        saveUser()

        assertFailsWith<UnauthorizedException> { service.login(email, "WrongPassword123") }
    }

    @Test
    fun `unknown email is rejected with UnauthorizedException`() = runTest {
        saveUser()

        assertFailsWith<UnauthorizedException> { service.login("other@mail.com", password) }
    }

    @Test
    fun `wrong password and unknown email give the same message`() = runTest {
        saveUser()

        val wrongPassword = assertFailsWith<UnauthorizedException> {
            service.login(email, "WrongPassword123")
        }
        val unknownEmail = assertFailsWith<UnauthorizedException> {
            service.login("other@mail.com", password)
        }

        assertEquals(wrongPassword.message, unknownEmail.message)
    }

    @Test
    fun `email lookup ignores casing`() = runTest {
        saveUser()

        val response = service.login("LOGIN@Mail.com", password)

        assertEquals(Role.COLLECTOR, response.role)
    }

    @Test
    fun `a removed user cannot log in`() = runTest {
        val saved = saveUser()
        repository.delete(saved.id)

        assertFailsWith<UnauthorizedException> { service.login(email, password) }
    }
}