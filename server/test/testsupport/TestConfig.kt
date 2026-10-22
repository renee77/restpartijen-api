package com.restpartijen.api.testsupport

import io.ktor.server.config.MapApplicationConfig

private val testValues = listOf(
    "jwt.secret" to "0123456789abcdef0123456789abcdef",
    "jwt.issuer" to "test-issuer",
    "jwt.audience" to "test-audience",
    "jwt.validityHours" to "1",
    "openFoodFacts.contactEmail" to "test@example.org",
)

/**
 * Config for integration tests. testApplication does not load application.yaml,
 * so every test that calls module() needs this.
 */
fun testConfig(): MapApplicationConfig = MapApplicationConfig(testValues)

/** The test config without [key], for a test that checks what happens when a setting is missing. */
fun testConfigWithout(key: String): MapApplicationConfig = MapApplicationConfig(testValues.filter { it.first != key })
