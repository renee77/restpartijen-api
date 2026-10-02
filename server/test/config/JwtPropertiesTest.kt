package com.restpartijen.api.config

import io.ktor.server.config.MapApplicationConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

class JwtPropertiesTest {
    // Exactly 32 characters = 32 bytes: the shortest secret that is accepted
    private val validSecret = "0123456789abcdef0123456789abcdef"

    @Test
    fun `all values are read from the jwt section`() {
        // Arrange
        val config = MapApplicationConfig(
            "jwt.secret" to validSecret,
            "jwt.issuer" to "test-issuer",
            "jwt.audience" to "test-audience",
            "jwt.validityHours" to "2",
        )
        // Act
        val properties = config.jwtProperties()
        // Assert
        assertEquals(JwtProperties(validSecret, "test-issuer", "test-audience", 2.hours), properties)
    }

    @Test
    fun `missing secret stops with a message that names JWT_SECRET`() {
        // Arrange: like testApplication, which does not load application.yaml
        val config = MapApplicationConfig()
        // Act
        val exception = assertFailsWith<IllegalStateException> { config.jwtProperties() }
        // Assert
        assertTrue("JWT_SECRET is not set" in exception.message.orEmpty())
    }

    @Test
    fun `blank secret is treated as missing`() {
        // Arrange: someone copied .env.example and left JWT_SECRET empty
        val config = MapApplicationConfig("jwt.secret" to "   ")
        // Act
        val exception = assertFailsWith<IllegalStateException> { config.jwtProperties() }
        // Assert: "not set", not "too short"
        assertTrue("JWT_SECRET is not set" in exception.message.orEmpty())
    }

    @Test
    fun `too short secret stops with a message that names JWT_SECRET`() {
        // Arrange
        val config = MapApplicationConfig("jwt.secret" to "too-short")
        // Act
        val exception = assertFailsWith<IllegalStateException> { config.jwtProperties() }
        // Assert
        assertTrue("JWT_SECRET is too short" in exception.message.orEmpty())
    }

    @Test
    fun `error message never contains the secret`() {
        // Arrange
        val shortSecret = "my-private-value"
        val config = MapApplicationConfig("jwt.secret" to shortSecret)
        // Act
        val exception = assertFailsWith<IllegalStateException> { config.jwtProperties() }
        // Assert
        assertFalse(shortSecret in exception.message.orEmpty())
    }

    @Test
    fun `toString hides the secret`() {
        // Arrange
        val properties = JwtProperties(validSecret, "test-issuer", "test-audience", 24.hours)
        // Act
        val text = properties.toString()
        // Assert
        assertFalse(validSecret in text)
        assertTrue("secret=***" in text)
    }

    //Made by Stefan
    @Test
    fun `missing values fall back to defaults`() {
        // Arrange
        val config = MapApplicationConfig("jwt.secret" to validSecret)
        // Act
        val properties = config.jwtProperties()
        // Assert
        assertEquals("restpartijen-api", properties.issuer)
        assertEquals("restpartijen-app", properties.audience)
        assertEquals(24.hours, properties.validity)
    }
}