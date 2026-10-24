package com.restpartijen.api.security.dto

import com.restpartijen.api.shared.Role
import kotlinx.serialization.Serializable

/**
 * Response body of POST /api/v1/auth/login (decision 3.8).
 *
 * @property token the signed JWT for the Authorization header
 * @property role the role of the user, so the app can pick its screens without decoding the token
 * @property supplierId the supplier of a SUPPLIER user (A15); null for every other role
 */
@Serializable
data class LoginResponse(val token: String, val role: Role, val supplierId: Long? = null)
