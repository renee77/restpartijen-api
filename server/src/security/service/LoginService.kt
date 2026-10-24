package com.restpartijen.api.security.service

import com.restpartijen.api.security.dto.LoginResponse
import com.restpartijen.api.security.repository.UserRepository
import com.restpartijen.api.shared.Role
import com.restpartijen.api.shared.SupplierIdLookup
import com.restpartijen.api.shared.UnauthorizedException

/**
 * Checks a user's credentials and issues a JWT (decision 3.8).
 *
 * An unknown email and a wrong password give exactly the same error,
 * so a caller cannot use login to find out which emails are registered.
 *
 * @param userRepository used to look up the user by email
 * @param passwordHasher verifies the plain password against the stored Argon2id hash
 * @param jwtConfig builds the signed token; the clock and validity come from its settings
 */
class LoginService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtConfig: JwtConfig,
    private val supplierIdLookup: SupplierIdLookup,
) {
    companion object {
        // One shared message for every failed login. Never make this depend on whether the email exists, or the error would leak that information.
        const val INVALID_CREDENTIALS = "Invalid email or password"
    }

    /**
     * Logs a user in.
     *
     * @return the token and the role of the user (the role is also in the token,
     *         but the app can show the right screens without decoding it)
     * @throws UnauthorizedException when the email is unknown, the user is removed,
     *         or the password is wrong (all three give the same message)
     */
    suspend fun login(email: String, password: String): LoginResponse {
        // findByEmail normalizes the email (trim + lowercase) and skips removed users,
        // so a removed user is treated exactly like an unknown one.
        val user = userRepository.findByEmail(email)

        // The password is checked only when the user exists. This is slightly faster for
        // unknown emails (timing difference); accepted for this project, see the PR notes.
        if (user == null || !passwordHasher.verifyPassword(password, user.passwordHash)) {
            throw UnauthorizedException(INVALID_CREDENTIALS)
        }

        // Only a supplier has a supplier id. For everyone else the lookup is not asked at all.
        val supplierId = if (user.role == Role.SUPPLIER) supplierIdLookup.lookupSupplierId(user.id) else null

        // Only the id and the role go into the token: no password, no email (section 16.1).
        return LoginResponse(
            token = jwtConfig.createToken(user.id, user.role),
            role = user.role,
            supplierId = supplierId,
        )
    }
}
