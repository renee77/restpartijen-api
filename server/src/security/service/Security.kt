package com.restpartijen.api.security.service

import com.restpartijen.api.shared.Role
import com.restpartijen.api.shared.UnauthorizedException
import io.ktor.server.application.Application
import io.ktor.server.auth.Authentication
import io.ktor.server.application.install
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

// Name of the JWT provider; routes refer to it via authenticate(PROVIDER_NAME).
const val PROVIDER_NAME = "jwt-auth"

fun Application.configureSecurity(jwtConfig: JwtConfig) {
    install(Authentication) {
        jwt(PROVIDER_NAME) {
            // The realm is used in the WWW-Authenticate header. It should be something identifiable to the app
            realm = "restpartijen API application"
            // The verifier is used to check the validity of incoming tokens. It is built using the same settings as the JwtConfig.
            verifier(jwtConfig.verifier)
            validate { credential ->
                val payload = credential.payload
                val userId = payload.getClaim(JwtClaims.USER_ID).asLong()
                val role = payload.getClaim(JwtClaims.ROLE).asString()

                // Expiry is checked against our injected Clock, not the system time (GI-6, besluit 6.5).
                // A token that is expired according to that clock is invalid: no principal, so 401.
                val expired = jwtConfig.isExpired(payload.expiresAt)

                // The userId must be present, the role must be a valid Role entry, and the token must not be expired.
                if (!expired && userId != null && role != null && Role.entries.any { it.name == role }) {
                    JWTPrincipal(payload)
                } else {
                    null
                }
            }
            // If the token is invalid or expired, respond with a 401 Unauthorized status and a message indicating the issue.
            challenge { _, _ ->
                throw UnauthorizedException("Authentication required")

            }
        }
    }
}