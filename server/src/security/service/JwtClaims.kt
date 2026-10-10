package com.restpartijen.api.security.service

import com.auth0.jwt.interfaces.Payload
import com.restpartijen.api.shared.Role

/** The claim names in our tokens and the one way to read them back (GI-3, decision 3.3). */
object JwtClaims {
    const val USER_ID = "userId"
    const val ROLE = "role"

    /** The user id in the token, or null when the claim is missing. */
    fun userId(payload: Payload): Long? = payload.getClaim(USER_ID).asLong()

    /** The role in the token, or null when the claim is missing or not a Role. */
    fun role(payload: Payload): Role? {
        val name = payload.getClaim(ROLE).asString()
        return Role.entries.firstOrNull { it.name == name }
    }
}