package com.restpartijen.api.testsupport

import com.restpartijen.api.security.model.User
import com.restpartijen.api.security.repository.UserRepository

class FakeUserRepository : UserRepository {
    val users = mutableListOf<User>()
    private var nextId = 1L

    private fun normalize(email: String) = email.trim().lowercase()

    override suspend fun findById(id: Long): User? =
        users.find { it.id == id && !it.deleted }

    override suspend fun findByEmail(email: String): User? =
        users.find { it.email == normalize(email) && !it.deleted }

    override suspend fun findAll(): List<User> = users.filter { !it.deleted }

    override suspend fun create(item: User): User {
        val saved = item.copy(id = nextId++, email = normalize(item.email))
        users.add(saved)
        return saved
    }

    override suspend fun update(item: User): Boolean = false // not used yet

    // Soft delete, like the real repository: the user stays, but is invisible.
    override suspend fun delete(id: Long): Boolean {
        val index = users.indexOfFirst { it.id == id && !it.deleted }
        if (index == -1) return false
        users[index] = users[index].copy(deleted = true)
        return true
    }
}