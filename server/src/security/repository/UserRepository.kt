package com.restpartijen.api.security.repository

import com.restpartijen.api.persistence.Repository
import com.restpartijen.api.security.model.User

interface UserRepository: Repository<User> {
    suspend fun findByEmail(email: String): User?
}