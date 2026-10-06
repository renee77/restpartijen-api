package com.restpartijen.api.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import org.slf4j.event.Level

/**
 * Logs every request for demonstration and debugging (ADR-05, §16.7).
 * Only method, path and status are logged: never passwords, tokens or the Authorization header.
 */
fun Application.configureCallLogging() {
    install(CallLogging) {
        level = Level.INFO
        format { call ->
            "${call.response.status()}: ${call.request.httpMethod.value} ${call.request.path()}"
        }
    }
}
