package com.restpartijen.api.security

import com.restpartijen.api.security.service.PasswordHasher
import com.restpartijen.api.security.service.RegistrationService
import com.restpartijen.api.security.service.RegistrationValidator
import com.restpartijen.api.shared.Role
import com.restpartijen.api.security.model.User
import com.restpartijen.api.shared.DomainRuleException
import com.restpartijen.api.testsupport.FakeUserRepository
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import com.restpartijen.api.testsupport.FixedClock
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.security.repository.ExposedUserRepository
import com.restpartijen.api.testsupport.RaceUserRepository
import kotlinx.coroutines.test.runTest
import com.restpartijen.api.shared.IllegalStateTransitionException
import com.restpartijen.api.shared.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class RegistrationServiceTest {
    private val fixedNow = Instant.parse("2026-01-15T10:30:00Z")
    private val repository = FakeUserRepository()
    private val service = RegistrationService(
        userRepository = repository,
        passwordHasher = PasswordHasher(),
        clock = FixedClock(fixedNow),
    )

    @Test
    fun `valid registration creates a COLLECTOR`() = runTest {
        // Arrange: valid input
        val email = "test@mail.com"

        // Act
        val user = service.register(
            email = email,
            password = "a".repeat(RegistrationValidator.PASSWORD_MIN_LENGTH),
            displayName = "TestName",
        )

        // Assert: the role is COLLECTOR and the user is really stored
        assertEquals(Role.COLLECTOR, user.role)
        assertEquals(email, user.email)
        assertEquals(user, repository.findByEmail(email))
    }

    @Test
    fun `stored password is a hash, not the plain password` () = runTest {
        // Arrange: Arranging valid input
        val email = "test@mail.com"
        val displayName = "TestName"
        val password = "TestPassword123"

        // Act: Register the user with the given information
        service.register(
            email = email,
            password = password,
            displayName = displayName
        )

        // Assert
        val savedUser = repository.findByEmail(email)

        assertNotEquals(savedUser?.passwordHash, password)
    }

    @Test
    fun `existing email is rejected` () = runTest {
        // Arrange, create a first user.
        service.register(
            email = "mail@test.com",
            password = "testPassword",
            displayName = "TestName"
        )

        // Act and assert: Create a second user with the same email
        assertFailsWith<IllegalStateTransitionException> {
            service.register(
                email = "mail@test.com",
                password = "testPassword123",
                displayName = "TestNameAnother"
            )
        }
    }

    @Test
    fun `existing email is rejected with different casing` () = runTest {
        // Arrange, create a first user.
        service.register(
            email = "mail@test.com",
            password = "testPassword",
            displayName = "TestName"
        )

        // Act and assert: Create a second user with the same email, different casing
        assertFailsWith<IllegalStateTransitionException> {
            service.register(
                email = "maIL@teSt.cOm",
                password = "testPassword123",
                displayName = "TestNameAnother"
            )
        }
    }

    @Test
    fun `invalid input is rejected and nothing is saved`() = runTest {
        // Arrange: a password that is too short, otherwise valid input
        val tooShort = "a".repeat(RegistrationValidator.PASSWORD_MIN_LENGTH - 1)

        // Act: registration must fail on validation
        val exception = assertFailsWith<ValidationException> {
            service.register(
                email = "test@mail.com",
                password = tooShort,
                displayName = "TestName",
            )
        }

        // Assert: the error points at the password, and nothing reached the repository
        assertEquals("password", exception.field)
        assertTrue(repository.users.isEmpty())
    }

    @Test
    fun `createdAt comes from the injected clock`() = runTest {
        // Act: register with the service built on the fixed clock
        val user = service.register(
            email = "test@mail.com",
            password = "a".repeat(RegistrationValidator.PASSWORD_MIN_LENGTH),
            displayName = "TestName",
        )

        // Assert: createdAt is exactly the fixed moment, in the returned user and in the stored one
        assertEquals(fixedNow, user.createdAt)
        assertEquals(fixedNow, repository.users.single().createdAt)
    }


    @Test
    fun `concurrent registration with the same email is rejected as a conflict`() = runTest {
        // Arrange: a real empty database where the email is already taken
        createEmptyTestDatabase(UsersTable)
        val real = ExposedUserRepository()
        real.create(
            User(
                id = 0,
                email = "test@mail.com",
                passwordHash = "existingHash",
                display_name = "Existing",
                role = Role.COLLECTOR,
                createdAt = fixedNow,
            )
        )
        val raceService = RegistrationService(
            userRepository = RaceUserRepository(real),
            passwordHasher = PasswordHasher(),
            clock = FixedClock(fixedNow),
        )

        // Act + Assert: findByEmail lies, the unique index fails, the service reports a conflict
        val exception = assertFailsWith<IllegalStateTransitionException> {
            raceService.register(
                email = "test@mail.com",
                password = "MyTestPassword123",
                displayName = "TestName",
            )
        }
        assertEquals(RegistrationService.EMAIL_TAKEN, exception.message)
    }
}