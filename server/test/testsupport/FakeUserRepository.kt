package com.restpartijen.api.testsupport

import com.restpartijen.api.security.model.User
import com.restpartijen.api.security.repository.UserRepository
import kotlin.time.Clock

/**
 * In-memory UserRepository for service tests.
 *
 * Normalizes the email and soft-deletes like the real repository, so lookups behave the same.
 * [update] is not implemented: it always returns false.
 */
class FakeUserRepository : UserRepository {
    /** The stored users, including soft-deleted ones; a test can fill or inspect it directly. */
    val users = mutableListOf<User>()

    /** The id for the next create, counting up from 1 like an auto-increment column. */
    private var nextId = 1L

    /** Stamps deletedAt on a soft delete. */
    private val clock: Clock = Clock.System

    /** Trim and lower case, as the real repository does. */
    private fun normalize(email: String) = email.trim().lowercase()

    override suspend fun findById(id: Long): User? = users.find { it.id == id && it.deletedAt == null }

    override suspend fun findByEmail(email: String): User? =
        users.find { it.email == normalize(email) && it.deletedAt == null }

    override suspend fun findAll(): List<User> = users.filter { it.deletedAt == null }

    override suspend fun create(item: User): User {
        val saved = item.copy(id = nextId++, email = normalize(item.email))
        users.add(saved)
        return saved
    }

    override suspend fun update(item: User): Boolean = false // not implemented, see the class KDoc

    // Soft delete, like the real repository: the user stays, but is invisible.
    override suspend fun delete(id: Long): Boolean {
        val index = users.indexOfFirst { it.id == id && it.deletedAt == null }
        if (index == -1) return false
        users[index] = users[index].copy(deletedAt = clock.now())
        return true
    }
}
