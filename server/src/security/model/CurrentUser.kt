package com.restpartijen.api.security.model

import com.restpartijen.api.shared.Role

data class CurrentUser (
    val id: Long,
    val role: Role
)