package com.restpartijen.api.reservation.routes

import com.restpartijen.api.reservation.dto.CreateReservationRequest
import com.restpartijen.api.reservation.dto.toResponse
import com.restpartijen.api.reservation.service.ReservationService
import com.restpartijen.api.security.service.currentUser
import com.restpartijen.api.security.service.requireRole
import com.restpartijen.api.shared.Role
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

/** F2 routes. The role rules for F2 endpoints are written here, per decision 3.6 (option B). */
fun Application.configureReservationRouting() {
    val service: ReservationService by dependencies
    routing {
        // Only a collector may reserve (§5.5); no token gives 401, another role 403
        requireRole(Role.COLLECTOR) {
            post("/api/v1/reservations") {
                val request = call.receive<CreateReservationRequest>()
                val reservation = service.reserve(request.productId, collectorId = call.currentUser().id)
                call.respond(HttpStatusCode.Created, reservation.toResponse())
            }

            get("/api/v1/reservations") {
                val reservations = service.findOwn(call.currentUser().id)
                val response = reservations.map { it.toResponse() }
                call.respond(response)
            }
        }

    }
}