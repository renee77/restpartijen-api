package com.restpartijen.api.pricing.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import com.restpartijen.api.security.service.requireRole
import com.restpartijen.api.shared.Role.ADMIN

import com.restpartijen.api.pricing.service.ExpiryScheduler

fun Application.configurePricingRouting() {
    val service: ExpiryScheduler by dependencies
    routing {
        requireRole(ADMIN) {
            post("/api/v1/admin/maintenance/expire") {
                val report = service.runMaintenance()
                call.respond(report)
            }
        }
    }
}