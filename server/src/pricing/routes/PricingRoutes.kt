package com.restpartijen.api.pricing.routes

import com.restpartijen.api.pricing.dto.toAdminResponse
import com.restpartijen.api.pricing.service.AdminProductService
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import com.restpartijen.api.security.service.requireRole
import com.restpartijen.api.shared.Role.ADMIN
import com.restpartijen.api.pricing.service.ExpiryScheduler
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ValidationException
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.delete
import io.ktor.server.routing.get

fun Application.configurePricingRouting() {
    val adminProductService: AdminProductService by dependencies
    val service: ExpiryScheduler by dependencies

    routing {
        requireRole(ADMIN) {
            post("/api/v1/admin/maintenance/expire") {
                val report = service.runMaintenance()
                call.respond(report)
            }
            get("/api/v1/admin/products") {
                // Repeated parameter: ?status=LISTED&status=RESERVED. No parameter gives an empty set.
                val statuses = call.request.queryParameters.getAll("status")
                    .orEmpty()
                    .map { it.toProductStatus() }
                    .toSet()

                val products = adminProductService.overview(statuses)
                call.respond(products.map { it.toAdminResponse() })
            }

            delete("/api/v1/admin/products/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: throw ValidationException("Product id must be a number", field = "id")

                adminProductService.delete(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

/** Turns query text into a status, or throws a 400 that lists the allowed values (§5.9). */
private fun String.toProductStatus(): ProductStatus =
    ProductStatus.entries.find { it.name == this }
        ?: throw ValidationException(
            "Unknown status '$this'. Allowed values: ${ProductStatus.entries.joinToString()}",
            field = "status"
        )