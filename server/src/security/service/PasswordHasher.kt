package com.restpartijen.api.security.service

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder

/**
 * Hashes and verifies passwords with Argon2id (decision 3.4).
 *
 * Parameters follow the OWASP Password Storage Cheat Sheet minimum for Argon2id:
 * 19 MiB memory, 2 iterations, parallelism 1.
 * Source: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
 * Each hash gets its own random salt, so the same password never gives the same hash.
 */
class PasswordHasher {
    private val encoder = Argon2PasswordEncoder(
        // salt length in bytes
        16,
        // hash length in bytes
        32,
        // parallelism
        1,
        // memory in KiB (19 MiB)
        19456,
        // iterations
        2,
    )

    /** Hashes the plain password and returns the encoded hash (starts with "$argon2id$"). */
    fun hashPassword(password: String): String =
        requireNotNull(encoder.encode(password)) { "Hashing returned no result" }

    /** Returns true when the plain password belongs to the given hash. */
    fun verifyPassword(password: String, hashedPassword: String): Boolean =
        encoder.matches(password, hashedPassword)
}