package com.restpartijen.api.testsupport

import com.restpartijen.api.security.model.User
import com.restpartijen.api.security.repository.ExposedUserRepository
import com.restpartijen.api.security.repository.UserRepository

/**
 * Simulates the moment two registrations race: findByEmail still sees nothing,
 * so the service goes on to create(), where the real unique index fails.
 * Everything else goes to the real repository (delegation).
 *
 * @param real the repository on the test database that does the actual work
 */
class RaceUserRepository(real: ExposedUserRepository) : UserRepository by real {
    override suspend fun findByEmail(email: String): User? = null
}
