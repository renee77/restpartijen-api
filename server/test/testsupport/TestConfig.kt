package com.restpartijen.api.testsupport

import io.ktor.server.config.MapApplicationConfig

/**
 * Config for integration tests. testApplication does not load application.yaml,
 * so every test that calls module() needs this.
 */
fun testConfig(): MapApplicationConfig = MapApplicationConfig(
    "jwt.secret" to "0123456789abcdef0123456789abcdef"
)