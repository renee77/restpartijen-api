package com.restpartijen.api.security.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val email: String, val displayName: String, val password: String) {
    override fun toString(): String = "UserRequest(email=$email, displayName=$displayName, password=***)"
}
