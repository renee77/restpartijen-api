package com.restpartijen.api.security.dto

import com.restpartijen.api.shared.Role
import kotlinx.serialization.Serializable

/**
 * Body of POST /auth/login (decision 3.8)
 */

@Serializable
data class LoginResponse(
    val token: String,
    val role: Role,
    val supplierId: Long? = null
    )