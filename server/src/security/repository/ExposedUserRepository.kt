package com.restpartijen.api.security.repository

import com.restpartijen.api.security.model.User
import com.restpartijen.api.persistence.dbQuery
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class ExposedUserRepository : UserRepository {
    override suspend fun findById(id: Long): User? = dbQuery {
            UsersTable
                .selectAll()
                .where { (UsersTable.id eq id) and notRemoved() }
                .singleOrNull()
                ?.toUser()
        }

    override suspend fun findByEmail(email: String): User? = dbQuery {
        UsersTable
            .selectAll()
            .where{ ( UsersTable.email eq email) and notRemoved() }
            .singleOrNull()
            ?.toUser()
    }

    override suspend fun findAll(): List<User> = dbQuery {
        UsersTable
            .selectAll()
            .where { notRemoved() }
            .map { it.toUser() }
    }

    override suspend fun create(item: User): User = dbQuery{
        // The ID gets created by the Database
        val newId = UsersTable.insert {
            it[UsersTable.email] = item.email
            it[UsersTable.password_hash] = item.passwordHash
            it[UsersTable.display_name] = item.display_name
            it[UsersTable.role] = item.role
            it[UsersTable.created_At] = item.createdAt
        }[UsersTable.id]
        item.copy(id = newId)
    }

    override suspend fun update(item: User): Boolean = dbQuery {
        // Update shows how many rows changed. 0 will make a false and means nothing was changed, or there was nothing to change.
        UsersTable.update( { (UsersTable.id eq item.id) and notRemoved() }) {
            it[UsersTable.email] = item.email
            it[UsersTable.password_hash] = item.passwordHash
            it[UsersTable.display_name] = item.display_name
            it[UsersTable.role] = item.role
            it[UsersTable.created_At] = item.createdAt
        } > 0
    }

    override suspend fun delete(id: Long): Boolean = dbQuery {
        // Soft delete
        UsersTable.update( { (UsersTable.id eq id) and notRemoved() }) {
            it[UsersTable.deleted] = true
        } > 0
    }
}

// The condition every query shares: a REMOVED product is invisible to the rest of the application.
private fun notRemoved(): Op<Boolean> = UsersTable.deleted eq false

// Translates the database into a User. The communicator between the database table and the object itself.
private fun ResultRow.toUser(): User = User(
    id = this[UsersTable.id],
    email = this[UsersTable.email],
    passwordHash = this[UsersTable.password_hash],
    display_name = this[UsersTable.display_name],
    role = this[UsersTable.role],
    createdAt = this[UsersTable.created_At],
    deleted = this[UsersTable.deleted]
)