package com.restpartijen.api.security.routes

import com.restpartijen.api.security.dto.LoginRequest
import com.restpartijen.api.security.dto.RegisterRequest
import com.restpartijen.api.security.dto.toResponse
import com.restpartijen.api.security.service.LoginService
import com.restpartijen.api.security.service.RegistrationService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.configureAuthRouting() {
    val service: RegistrationService by dependencies
    val loginService: LoginService by dependencies
    routing {
        post("/api/v1/auth/register") {
            val request = call.receive<RegisterRequest>()

            val user = service.register(
                email = request.email,
                password = request.password,
                display_name = request.displayName,
            )

            call.respond(
                HttpStatusCode.Created,
                user.toResponse(),
            )
        }
    }

    routing {
        post("/api/v1/auth/login") {
            val request = call.receive<LoginRequest>()

            val response = loginService.login(request.email, request.password)

            call.respond(HttpStatusCode.OK, response)
        }
    }
}
