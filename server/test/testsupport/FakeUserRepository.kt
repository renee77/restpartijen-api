package com.restpartijen.api.testsupport

import com.restpartijen.api.security.model.User
import com.restpartijen.api.security.repository.UserRepository

open class FakeUserRepository : UserRepository {
    val users = mutableListOf<User>()
    private var nextId = 1L

    override suspend fun findById(id: Long): User? = users.find { it.id == id }
    override suspend fun findByEmail(email: String): User? =
        users.find { it.email == email.trim().lowercase() }
    override suspend fun findAll(): List<User> = users.toList()
    override suspend fun create(item: User): User {
        val saved = item.copy(id = nextId++, email = item.email.trim().lowercase())
        users.add(saved)
        return saved
    }
    override suspend fun update(item: User): Boolean = false // not used in registration
    override suspend fun delete(id: Long): Boolean = false   // not used in registration
}