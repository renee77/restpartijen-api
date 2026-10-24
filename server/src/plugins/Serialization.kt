package com.restpartijen.api.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

/**
 * Reads and writes request and response bodies as JSON with kotlinx.serialization.
 *
 * Keeps Ktor's default Json settings on purpose: ignoreUnknownKeys stays off, so a field the request model
 * does not know makes the body unreadable and StatusPages answers 400 (B-49).
 */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}
