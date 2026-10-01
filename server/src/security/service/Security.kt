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
            realm = "use IT too application"
            // The verifier is used to check the validity of incoming tokens. It is built using the same settings as the JwtConfig.
            verifier(jwtConfig.verifier)
            validate { credential ->
                val payload = credential.payload
                val userId = payload.getClaim("userId").asLong()
                val role = payload.getClaim("role").asString()

                // Check if the userId is not null and the role is a valid Role enum entry. If so, return a JWTPrincipal with the payload; otherwise, return null to indicate invalid credentials.
                if (userId != null && role != null && Role.entries.any { it.name == role }) {
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