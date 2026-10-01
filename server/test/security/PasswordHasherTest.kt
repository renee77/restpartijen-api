package com.restpartijen.api.security

import com.restpartijen.api.security.service.PasswordHasher
import kotlin.test.Test
import kotlin.test.assertEquals

class PasswordHasherTest {
    // Happy path: Hash a password and it gets verified succesfully.
    @Test
    fun `test hash and verify password` () {
        // Arrange: get the password hasher and a sample password.
        val hash = PasswordHasher()
        val password = "secretPassword123!"

        // Act: Hash the password.
        val hashedPassword = hash.hashPassword(password)

        // Assert: Verify that the hashed password matches the original password.
        assertEquals(true, hash.verifyPassword(password, hashedPassword))
    }
}