package com.restpartijen.api.security.dto

import kotlinx.serialization.Serializable

/**
 * Request body of POST /api/v1/auth/register. RegistrationValidator checks the fields (A11).
 *
 * @property email the e-mail address for the new account
 * @property displayName the name shown for the account
 * @property password the plain password; only its Argon2id hash is stored
 */
@Serializable
data class RegisterRequest(val email: String, val displayName: String, val password: String) {
    /** Shows the password as `***`, so logging the request object does not reveal it. */
    override fun toString(): String = "RegisterRequest(email=$email, displayName=$displayName, password=***)"
}
