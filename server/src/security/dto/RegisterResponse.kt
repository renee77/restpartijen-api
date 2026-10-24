package com.restpartijen.api.security.dto

import com.restpartijen.api.security.model.User
import com.restpartijen.api.shared.Role
import kotlinx.serialization.Serializable

/**
 * JSON response for a single User.
 *
 * Kept separate from the domain model so the API contract can change without touching User, and vice versa.
 */

@Serializable
data class UserResponse(val id: Long, val email: String, val displayName: String, val role: Role)

fun User.toResponse(): UserResponse = UserResponse(
    id = id,
    email = email,
    displayName = display_name,
    role = role,
)
