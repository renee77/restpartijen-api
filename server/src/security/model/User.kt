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
) {
    /**
     * Shows the password hash as `***`, so logging the user object does not reveal it;
     * the generated data-class toString would print the hash. The other fields keep the data-class format.
     */
    override fun toString(): String =
        "User(id=$id, email=$email, passwordHash=***, displayName=$displayName, role=$role, " +
            "createdAt=$createdAt, deletedAt=$deletedAt)"
}
