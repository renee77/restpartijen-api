package com.restpartijen.api

import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

/**
 * Entry point of the application.
 * Hands control to Ktor's Netty EngineMain, which reads resources/application.yaml
 * for the port and the module(s) to load.
 */
fun main(args: Array<String>) {
    EngineMain.main(args)
}

/**
 * The single place where the application is assembled (GI-2, decision 2.2).
 * Referenced by name in application.yaml: com.restpartijen.api.ApplicationKt.module
 * Plugins and feature routing are added here in the next steps.
 */
fun Application.module() {
    // Intentionally empty for now; configureSerialization() follows in step 3c.
}