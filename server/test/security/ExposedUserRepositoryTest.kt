package com.restpartijen.api.security

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.restpartijen.api.testsupport.FixedClock
import com.restpartijen.api.security.model.User
import com.restpartijen.api.security.repository.ExposedUserRepository
import com.restpartijen.api.security.repository.UsersTable
import com.restpartijen.api.shared.Role
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * Integration test: the real repository against a real, empty H2 database per test (E4).
 * Shows table, repository, transaction and test database working together.
 */

class ExposedUserRepositoryTest {
    val newUser = User(
        id=0,
        email="testmail@mail.com", passwordHash="passwordHash", display_name="TestName",
        role=Role.COLLECTOR,
        createdAt = FixedClock(
            Instant.parse("2026-01-15T10:30:00Z")
        ).now().toLocalDateTime(TimeZone.UTC),
        deleted = false
    )

    @Test
    fun `create stores the new User, and findById returns it` () = runTest {
        // Arrange: Create a test database using the Users table and use the Exposed User Repository.
        createEmptyTestDatabase(UsersTable)
        val repository = ExposedUserRepository()

        // Act
        val user = repository.create(newUser)

        // Assert
        assertEquals(user, repository.findById(user.id))
    }

    @Test
    fun `findByEmail returns null when the email does not exist`() = runTest {
        // Arrange
        createEmptyTestDatabase(UsersTable)
        val repository = ExposedUserRepository()

        // Act
        val user = repository.findByEmail("testmail")

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
}