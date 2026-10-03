package com.restpartijen.api.security.service

import com.restpartijen.api.security.model.CurrentUser
import com.restpartijen.api.shared.Role
import com.restpartijen.api.shared.UnauthorizedException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

/**
 * Returns the user making this request (id and role), read from the JWTPrincipal.
 * Only works in routes protected by requireRole/authenticate; elsewhere there is no principal.
 * It throws instead of returning null, so handlers never have to null-check.
 */
fun ApplicationCall.currentUser(): CurrentUser {
    // Get the JWTPrincipal from the call. If it's null, throw UnauthorizedException.
    val principal = principal<JWTPrincipal>()
        ?: throw UnauthorizedException("Authentication required")

    val userId = principal.payload.getClaim("userId").asLong()
    val roleName = principal.payload.getClaim("role").asString()
    val role = Role.entries.firstOrNull { it.name == roleName }

    // After validate() this cannot happen for a genuine token, but the compiler doesn't know that.
    if (userId == null || role == null) {
        throw UnauthorizedException("Authentication required")
    }

    return CurrentUser(id = userId, role = role)
}