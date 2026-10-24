package com.restpartijen.api.security.dto

import com.restpartijen.api.security.model.User
import com.restpartijen.api.shared.Role
import kotlinx.serialization.Serializable

/**
 * JSON response for a single User.
 *
 * Kept separate from the domain model so the API contract can change without touching User, and vice versa.
 * It leaves out the password hash and the timestamps.
 *
 * @property id the user id
 * @property email the normalized e-mail address
 * @property displayName the name shown for the account
 * @property role the role of the account
 */

@Serializable
data class UserResponse(val id: Long, val email: String, val displayName: String, val role: Role)

/** Maps a user to its JSON answer, without the password hash. */
fun User.toResponse(): UserResponse = UserResponse(
    id = id,
    email = email,
    displayName = displayName,
    role = role,
)
