package com.restpartijen.api.security.service

import com.restpartijen.api.shared.ForbiddenException
import com.restpartijen.api.shared.Role
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route

class RoleCheckConfig {
    var roles: Set<Role> = setOf()
}

// Runs right after authentication. A role that is not allowed is thrown as ForbiddenException;
// StatusPages turns it into a 403.
private val RoleCheck = createRouteScopedPlugin("RoleCheck", ::RoleCheckConfig) {
    val allowedRoles = pluginConfig.roles
    require(allowedRoles.isNotEmpty()) { "requireRole needs at least one role" }

    on(AuthenticationChecked) { call ->
        // No principal means authentication failed; the challenge already answers that with a 401.
        val principal = call.principal<JWTPrincipal>() ?: return@on

        // The claim is text; look up the matching Role. Unknown or missing text gives null.
        val roleName = principal.payload.getClaim("role").asString()
        val role = Role.entries.firstOrNull { it.name == roleName }

        if (role == null || role !in allowedRoles) {
            throw ForbiddenException("Insufficient role")
        }
    }
}
// A function to require a role on a route.
// It wraps the route in an authenticate block and installs the RoleCheck plugin.
// A vararg is used because a route can require multiple roles. The build block is the route's content.
fun Route.requireRole(vararg roles: Role, build: Route.() -> Unit) {
    // Wrap the route in an authenticate block and install the RoleCheck plugin with the required roles.
    authenticate {
        install(RoleCheck) {
            this.roles = roles.toSet()
        }
        build()
    }
}