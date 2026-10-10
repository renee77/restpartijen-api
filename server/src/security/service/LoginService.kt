package com.restpartijen.api.security.service

import com.restpartijen.api.security.dto.LoginResponse
import com.restpartijen.api.security.repository.UserRepository
import com.restpartijen.api.shared.UnauthorizedException

/**
 * Checks credentials and issues a token (decision 3.8).
 * An unknown email and a wrong password give the same error, so nobody can find out which emails exist.
 */
class LoginService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtConfig: JwtConfig,
) {
    companion object {
        const val INVALID_CREDENTIALS = "Invalid email or password"
    }

    suspend fun login(email: String, password: String): LoginResponse {
        val user = userRepository.findByEmail(email)

        if (user == null || !passwordHasher.verifyPassword(password, user.passwordHash)) {
            throw UnauthorizedException(INVALID_CREDENTIALS)
        }

        return LoginResponse(
            token = jwtConfig.createToken(user.id, user.role),
            role = user.role,
        )
    }
}