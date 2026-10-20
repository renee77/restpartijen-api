package com.restpartijen.api.security.repository

import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.security.model.User
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock

class ExposedUserRepository(
    private val clock: Clock = Clock.System,
) : UserRepository {

    override suspend fun findById(id: Long): User? = dbQuery {
        UsersTable.selectAll()
            .where { (UsersTable.id eq id) and notRemoved() }
            .singleOrNull()
            ?.toUser()
    }

    override suspend fun findByEmail(email: String): User? = dbQuery {
        UsersTable.selectAll()
            .where { (UsersTable.email eq normalizeEmail(email)) and notRemoved() }
            .singleOrNull()
            ?.toUser()
    }

    override suspend fun findAll(): List<User> = dbQuery {
        UsersTable.selectAll()
            .where { notRemoved() }
            .map { it.toUser() }
    }

    override suspend fun create(item: User): User = dbQuery {
        val email = normalizeEmail(item.email)
        val newId = UsersTable.insert {
            it[UsersTable.email] = email
            it[passwordHash] = item.passwordHash
            it[displayName] = item.display_name
            it[role] = item.role
            it[createdAt] = item.createdAt
        }[UsersTable.id]
        // Return what is really stored: the generated id and the normalized email.
        item.copy(id = newId, email = email)
    }

    override suspend fun update(item: User): Boolean = dbQuery {
        // 0 changed rows means: nothing found, or nothing to change.
        UsersTable.update({ (UsersTable.id eq item.id) and notRemoved() }) {
            it[email] = normalizeEmail(item.email)
            it[passwordHash] = item.passwordHash
            it[displayName] = item.display_name
            it[role] = item.role
            it[createdAt] = item.createdAt
        } > 0
    }

    override suspend fun delete(id: Long): Boolean = dbQuery {
        // Soft delete: the row stays, but gets a deleted_at timestamp and is invisible to every query.
        UsersTable.update({ (UsersTable.id eq id) and notRemoved() }) {
            it[deletedAt] = clock.now()
        } > 0
    }
}

// The one place where an email is normalized, so lookups and saves always agree.
private fun normalizeEmail(email: String): String = email.trim().lowercase()

// The condition every query shares: a removed user (deleted_at is set) is invisible to the rest of the application.
private fun notRemoved(): Op<Boolean> = UsersTable.deletedAt.isNull()

// Translates a database row into a User.
private fun ResultRow.toUser(): User = User(
    id = this[UsersTable.id],
    email = this[UsersTable.email],
    passwordHash = this[UsersTable.passwordHash],
    display_name = this[UsersTable.displayName],
    role = this[UsersTable.role],
    createdAt = this[UsersTable.createdAt],
    deleted_at = this[UsersTable.deletedAt],
)