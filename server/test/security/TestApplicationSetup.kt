package com.restpartijen.api.security

import com.restpartijen.api.security.service.JwtConfig
import com.restpartijen.api.security.service.PROVIDER_NAME
import com.restpartijen.api.security.service.configureSecurity
import com.restpartijen.api.security.service.currentUser
import com.restpartijen.api.security.service.requireRole
import com.restpartijen.api.shared.ForbiddenException
import com.restpartijen.api.shared.Role
import com.restpartijen.api.shared.UnauthorizedException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

val jwtConfig = JwtConfig(TestJwtSettings.default, TestClock.fixed)


// TEMPORARY TESTING SETUP: Configure status pages to handle exceptions and respond with appropriate HTTP status codes. This is used in the test application to simulate production error handling.
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<UnauthorizedException> { call, _ ->
            call.respond(HttpStatusCode.Unauthorized)
        }
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError)
        }
        exception<ForbiddenException> { call, _ ->
            call.respond(HttpStatusCode.Forbidden)
        }
    }
}

// Test app: same error handling as production, security is installed, one protected route.
fun Application.setUpTestApp() {
    configureStatusPages()
    configureSecurity(jwtConfig)
    // One protected route for testing purposes. It requires authentication via the JWT provider.
    routing {
        authenticate(PROVIDER_NAME) {
            get("/test/protected") { call.respondText("ok") }
        }

        // A route that requires a specific role (COLLECTOR) for testing the role-based access control.
        requireRole(Role.COLLECTOR) {
            get("/test/role-protected-collector") { call.respondText("ok") }
        }

        // A route that can have a current user with any role, for testing purposes.
        authenticate(PROVIDER_NAME) {
            get("/test/current-user") {
                val currentUser = call.currentUser()
                call.respondText("ok, role: ${currentUser.role} and id: ${currentUser.id}")
            }
        }
    }
}