package com.restpartijen.api.security

import com.restpartijen.api.security.service.PasswordHasher
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class PasswordHasherTest {
    // Happy path: Hash a password and it gets verified succesfully.
    @Test
    fun `test the right password` () {
        // Arrange: get the password hasher and a sample password.
        val hash = PasswordHasher()
        val password = "secretPassword123!"

        // Act: Hash the password.
        val hashedPassword = hash.hashPassword(password)

        // Assert: Verify that the hashed password matches the original password.
        assertTrue(hash.verifyPassword(password, hashedPassword))
}


    // Sad path: Verify a password against a wrong hash.
    @Test
    fun `test the password with wrong hash` () {
        // Arrange: get the password hasher and a sample password.
        val hash = PasswordHasher()
        val password = "secret456!"

        // Act: Hash the password.
        val hashedPassword = hash.hashPassword(password)

        // Assert: Verify that the hashed password does not match a different password.
        assertFalse(hash.verifyPassword("wrongPassword", hashedPassword))
    }

    // Check that a password is not hashed to the same value every time (salting).
    @Test
    fun `test that hashing the same password twice produces different hashes` () {
        // Arrange: get the password hasher and a sample password.
        val hash = PasswordHasher()
        val password = "secretPassword123!"

        // Act: Hash the password two times.
        val hash1 = hash.hashPassword(password)
        val hash2 = hash.hashPassword(password)

        // Assert: The two hashes should not be equal, because the hashing process includes salting.
        assertNotEquals(hash1, hash2)
    }
}