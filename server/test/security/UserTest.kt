package com.restpartijen.api.security

import com.restpartijen.api.security.model.User
import com.restpartijen.api.shared.Role
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

/** User.toString names the class and keeps the password hash out of logs. */
class UserTest {
    @Test
    fun `toString names User and masks the password hash`() {
        // Arrange: a user with a recognizable hash
        val hash = "\$argon2id\$v=19\$m=65536,t=3,p=1\$SaltValue\$HashValue"
        val user = User(
            id = 7,
            email = "test@mail.com",
            passwordHash = hash,
            displayName = "TestName",
            role = Role.COLLECTOR,
            createdAt = Instant.parse("2026-01-15T10:30:00Z"),
        )

        // Act
        val text = user.toString()

        // Assert: the right class, the hash only as ***, the other fields still readable.
        // The messages leave out the text, so a failure does not print the test hash.
        assertTrue(text.startsWith("User("), "toString does not start with User(")
        assertTrue(text.contains("passwordHash=***"), "toString does not show passwordHash=***")
        assertFalse(text.contains(hash), "toString leaked the password hash")
        assertTrue(text.contains("test@mail.com"), "toString does not show the email")
        assertTrue(text.contains("TestName"), "toString does not show the displayName")
    }
}
