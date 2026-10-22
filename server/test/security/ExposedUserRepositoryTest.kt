package com.restpartijen.api.security

import com.restpartijen.api.security.model.User
import com.restpartijen.api.security.repository.ExposedUserRepository
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertFailsWith
import kotlin.time.Instant

/**
 * Integration test: the real repository against a real, empty H2 database per test (E4).
 * Shows table, repository, transaction and test database working together.
 */

class ExposedUserRepositoryTest {
    private val fixedNow = Instant.parse("2026-01-15T10:30:00Z")
    val newUser = User(
        id=0,
        email="testmail@mail.com", passwordHash="passwordHash", display_name="TestName",
        role=Role.COLLECTOR,
        createdAt = fixedNow,
    )

    @Test
    fun `create stores the new User, and findById returns it` () = runTest {
        // Arrange: Create a test database using the Users table and use the Exposed User Repository.
        createEmptyTestDatabase(UsersTable)
        val repository = ExposedUserRepository()

        // Act
        val user = repository.create(newUser)

        // Assert
        assertNotEquals(0L, user.id)
        assertEquals(user, repository.findById(user.id))
    }

    @Test
    fun `findByEmail returns null when the email does not exist`() = runTest {
        // Arrange
        createEmptyTestDatabase(UsersTable)
        val repository = ExposedUserRepository()

        // Act
        val user = repository.findByEmail("testmail@mail.com")

        // Assert
        assertNull(user)
    }

    @Test
    fun `findByEmail return User when the email exists` () = runTest {
        // Arrange: Create a test database using the Users table and use the Exposed User Repository.
        createEmptyTestDatabase(UsersTable)
        val repository = ExposedUserRepository()

        // Act
        val user = repository.create(newUser)

        // Assert
        assertEquals(user, repository.findByEmail(user.email))
    }

    @Test
    fun `findById returns null when the id is unknown` () = runTest {
        // Arrange: Create a test Database
        createEmptyTestDatabase(UsersTable)
        val repository = ExposedUserRepository()

        // Act
        val nonUser = repository.findById(99L)

        // Assert
        assertNull(nonUser)
    }

    @Test
    fun `create rejects a duplicate emailaddress` () = runTest {
        // Arrange: Create a test Database
        createEmptyTestDatabase(UsersTable)
        val repository = ExposedUserRepository()

        // Act
        val user = repository.create(newUser)
        val secondUser = newUser

        // Assert
        assertFailsWith<EmailAlreadyExistsException> {
            repository.create(secondUser)
        }
    }

    @Test
    fun `email is stored lowercase` () = runTest {
        // Arrange
        createEmptyTestDatabase(UsersTable)
        val repository = ExposedUserRepository()

        // Act
        val user = repository.create(newUser)
        val email = "TeSTmail@maIL.com"

        assertEquals(user, repository.findByEmail(email))
    }

    @Test
    fun `create stores the password hash not the plain password`() = runTest {
        // Arrange
        createEmptyTestDatabase(UsersTable)
        val repository = ExposedUserRepository()

        val plainPassword = "TestPassword123!"
        val passwordHash = "test-hash-value"

        val user = User(
            id = 0,
            email = "test@example.nl",
            passwordHash = passwordHash,
            display_name = "Test User",
            role = Role.COLLECTOR,
            createdAt = fixedNow,
        )

        // Act
        val createdUser = repository.create(user)

        // Assert: read the stored password hash directly from the database
        val storedPasswordHash = transaction {
            UsersTable.selectAll()
                .where { UsersTable.id eq createdUser.id }
                .single()[UsersTable.passwordHash]
        }

        assertEquals(passwordHash, storedPasswordHash)
        assertNotEquals(plainPassword, storedPasswordHash)
    }

    @Test
    fun `create stores the email trimmed and in lowercase`() = runTest {
        createEmptyTestDatabase(UsersTable)
        val repository = ExposedUserRepository()

        val user = repository.create(newUser)

        assertEquals("testmail@mail.com", user.email)
        assertEquals(user, repository.findByEmail("TESTMAIL@mail.com"))
    }

    @Test
    fun `findById returns null for an unknown id`() = runTest {
        createEmptyTestDatabase(UsersTable)

        assertNull(ExposedUserRepository().findById(999))
    }

    @Test
    fun `create rejects a duplicate email`() = runTest {
        createEmptyTestDatabase(UsersTable)
        val repository = ExposedUserRepository()
        repository.create(newUser)

        assertFailsWith<EmailAlreadyExistsException> { repository.create(newUser) }
    }
}