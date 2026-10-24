package com.restpartijen.api.security.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String) {
    override fun toString(): String = "UserRequest(email=$email, password=***)"
}
