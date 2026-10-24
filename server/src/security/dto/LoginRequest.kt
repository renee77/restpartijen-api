package com.restpartijen.api.security.dto

import kotlinx.serialization.Serializable

/**
 * Request body of POST /api/v1/auth/login (decision 3.8).
 *
 * @property email the e-mail address of the account; the lookup trims and lowercases it
 * @property password the plain password, checked against the stored hash
 */
@Serializable
data class LoginRequest(val email: String, val password: String) {
    /** Shows the password as `***`, so logging the request object does not reveal it. */
    override fun toString(): String = "LoginRequest(email=$email, password=***)"
}
