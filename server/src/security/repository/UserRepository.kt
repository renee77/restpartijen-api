package com.restpartijen.api.security.repository

import com.restpartijen.api.persistence.Repository
import com.restpartijen.api.security.model.User

/** Persistence of user accounts; implements the generic contract from GI-1 plus a lookup by e-mail. */
interface UserRepository : Repository<User> {
    /** The user with this e-mail address, compared trimmed and case-insensitive, or null if unknown or removed. */
    suspend fun findByEmail(email: String): User?
}
