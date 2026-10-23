package com.restpartijen.api.reservation.routes

import com.restpartijen.api.reservation.dto.toResponse
import com.restpartijen.api.reservation.model.SearchCriteria
import com.restpartijen.api.reservation.service.ProductSearchService
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ValidationException
import io.ktor.http.Parameters
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlin.time.Duration.Companion.hours

/** F2: searching the offer (US-04). Open to everyone, also without a token (§5.4). */
fun Application.configureSearchRouting() {
    val service: ProductSearchService by dependencies
    routing {
        get("/api/v1/products") {
            val criteria = call.request.queryParameters.toSearchCriteria()
            call.respond(service.search(criteria).map { it.toResponse() })
        }
    }
}
/** Turns the query text into typed filters; wrong input gives a 400 that names the parameter. */
private fun Parameters.toSearchCriteria() = SearchCriteria(
    category = this["category"]?.let { text ->
        ProductCategory.entries.find { it.name == text }
            ?: throw ValidationException(
                "Unknown category '$text'. Allowed values: ${ProductCategory.entries.joinToString()}",
                field = "category"
            )
    },
    maxPrice = this["maxPrice"]?.let { text ->
        val euros = text.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 }
            ?: throw ValidationException("maxPrice must be an amount in euros of 0 or more, for example 3.00", field = "maxPrice")
        Money.fromEuros(euros)
    },
    minShelfLife = this["minShelfLifeHours"]?.let { text ->
        val hours = text.toLongOrNull()?.takeIf { it >= 0 }
            ?: throw ValidationException("minShelfLifeHours must be a whole number of 0 or more", field = "minShelfLifeHours")
        hours.hours
    }
)