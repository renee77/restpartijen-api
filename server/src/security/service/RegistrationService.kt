package com.restpartijen.api.security.service

import com.restpartijen.api.security.EmailAlreadyExistsException
import com.restpartijen.api.security.model.User
import com.restpartijen.api.security.repository.UserRepository
import com.restpartijen.api.shared.Role
import kotlin.time.Clock

class RegistrationService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val clock: Clock,
) {
    suspend fun register(email: String, password: String, display_name: String): User {
        RegistrationValidator.validate(email, password, display_name)

        // Fast path: gives a clear conflict without touching the insert.
        if (userRepository.findByEmail(email) != null) {
            throw EmailAlreadyExistsException(email)
        }

        val user = User(
            id = 0,
            email = email,
            passwordHash = passwordHasher.hashPassword(password),
            display_name = display_name,
            role = Role.COLLECTOR,
            createdAt = clock.now(),
        )

        // Two registrations can pass the check above at the same time.
        // The repository translates the unique index failure into the same exception.
        return userRepository.create(user)
    }
}