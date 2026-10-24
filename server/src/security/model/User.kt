package com.restpartijen.api.security.model

import com.restpartijen.api.shared.Role
import kotlin.time.Instant

/**
 * A user account (ERD §9.1, table USERS).
 *
 * @property id the user id; 0 for a user that is not stored yet
 * @property email the e-mail address; the repository stores it trimmed and lowercased
 * @property passwordHash the Argon2id hash of the password (decision 3.4); never the password itself
 * @property displayName the name shown for the account
 * @property role what the user may do
 * @property createdAt the moment the account was created
 * @property deletedAt the moment the account was soft-deleted; null while it is active
 */
data class User(
    val id: Long,
    val email: String,
    val passwordHash: String,
    val displayName: String,
    val role: Role,
    val createdAt: Instant,
    val deletedAt: Instant? = null,
)
