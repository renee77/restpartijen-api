package com.restpartijen.api.security.service

import com.restpartijen.api.security.model.User
import com.restpartijen.api.security.repository.UserRepository
import com.restpartijen.api.shared.IllegalStateTransitionException
import com.restpartijen.api.shared.Role
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import java.sql.SQLException
import kotlin.time.Clock

class RegistrationService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val clock: Clock,
) {
    companion object {
        const val EMAIL_TAKEN = "Email is already registered"

        // SQL state for a unique constraint violation (H2 and PostgreSQL).
        private const val UNIQUE_VIOLATION = "23505"
    }

    suspend fun register(email: String, password: String, displayName: String): User {
        RegistrationValidator.validate(email, password, displayName)

        if (userRepository.findByEmail(email) != null) {
            throw IllegalStateTransitionException(EMAIL_TAKEN)
        }

        val user = User(
            id = 0,
            email = email,
            passwordHash = passwordHasher.hashPassword(password),
            display_name = displayName,
            role = Role.COLLECTOR,
            createdAt = clock.now(),
        )

        // Two registrations can pass the check above at the same time.
        // The unique index is the real guard; its failure becomes the same conflict.
        return try {
            userRepository.create(user)
        } catch (e: ExposedSQLException) {
            val state = (e.cause as? SQLException)?.sqlState ?: e.sqlState
            if (state == UNIQUE_VIOLATION) throw IllegalStateTransitionException(EMAIL_TAKEN)
            throw e
        }
    }
}