package com.restpartijen.api.security.model

import com.restpartijen.api.shared.Role
import kotlinx.datetime.LocalDateTime

data class User(
    val id: Long,
    val email: String,
    val passwordHash: String,
    val display_name: String,
    val role: Role,
    val createdAt: LocalDateTime,
    val deleted: Boolean
)
