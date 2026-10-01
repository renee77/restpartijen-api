package com.restpartijen.api.security.service

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder

class PasswordHasher {
    private val encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

    // Hashes the provided password using Argon2id algorithm and returns the hashed password as a string.
    fun hashPassword(password: String): String {
        // Use requireNotNull to ensure that the result of the encoding is not null. If it is null, throw an exception with a message.
        return requireNotNull(encoder.encode(password)) { "Hashing returned no result" }
    }

    // Verifies if the provided password matches the hashed password.
    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return encoder.matches(password, hashedPassword)
    }
}