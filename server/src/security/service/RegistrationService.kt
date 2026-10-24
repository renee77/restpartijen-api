package com.restpartijen.api.security.service

import com.restpartijen.api.security.model.User
import com.restpartijen.api.security.repository.UserRepository
import com.restpartijen.api.shared.ConflictException
import com.restpartijen.api.shared.Role
import kotlin.time.Clock

class RegistrationService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val clock: Clock,
) {
    companion object {
        // Same message for the fast path here and the unique index check in the repository.
        const val EMAIL_TAKEN = "Email is already registered"
    }

    suspend fun register(email: String, password: String, displayName: String): User {
        RegistrationValidator.validate(email, password, displayName)

        // Fast path: gives a clear conflict without touching the insert.
        if (userRepository.findByEmail(email) != null) {
            throw ConflictException(EMAIL_TAKEN)
        }

        val user = User(
            id = 0,
            email = email,
            passwordHash = passwordHasher.hashPassword(password),
            displayName = displayName,
            role = Role.COLLECTOR,
            createdAt = clock.now(),
        )

        // Two registrations can pass the check above at the same time.
        // The repository translates the unique index failure into the same exception.
        return userRepository.create(user)
    }
}
