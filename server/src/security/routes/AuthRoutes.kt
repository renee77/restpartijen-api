package com.restpartijen.api.security.routes

import com.restpartijen.api.security.service.RegistrationService
import com.restpartijen.api.security.dto.toResponse
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import com.restpartijen.api.security.dto.RegisterRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond

fun Application.configureAuthRouting () {
    val service: RegistrationService by dependencies
    routing {
        post("/api/v1/auth/register") {
            val request = call.receive<RegisterRequest>()

            val user = service.register(
                email = request.email,
                password = request.password,
                displayName = request.displayName
            )

            call.respond(
                HttpStatusCode.Created,
                user.toResponse()
            )
        }
    }
}