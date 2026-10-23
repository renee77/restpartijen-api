package com.restpartijen.api.product

import com.restpartijen.api.product.client.OpenFoodFactsSettings
import com.restpartijen.api.product.client.openFoodFactsSettings
import io.ktor.server.config.MapApplicationConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Unit test: reading OFF_CONTACT_EMAIL, the contact in the User-Agent (§10.3, A5; security review M-4).
 * Like JWT_SECRET, a missing value stops the application at startup with a clear message (B-52).
 */
class OpenFoodFactsSettingsTest {

    @Test
    fun `openFoodFactsSettings reads the contact address`() {
        // Arrange
        val config = MapApplicationConfig("openFoodFacts.contactEmail" to " team@example.org ")

        // Act + Assert: surrounding spaces from a .env file do not end up in the header
        assertEquals(OpenFoodFactsSettings("team@example.org"), config.openFoodFactsSettings())
    }

    @Test
    fun `openFoodFactsSettings stops with a clear message when OFF_CONTACT_EMAIL is missing`() {
        // Arrange
        val config = MapApplicationConfig()

        // Act
        val error = assertFailsWith<IllegalStateException> { config.openFoodFactsSettings() }

        // Assert: the message names the variable, so whoever starts the app knows what to set
        assertTrue(error.message.orEmpty().contains("OFF_CONTACT_EMAIL"), error.message)
    }

    @Test
    fun `openFoodFactsSettings refuses a value with a line break`() {
        // Arrange
        // The address goes into a header; a line break could add a header of its own.
        val config = MapApplicationConfig("openFoodFacts.contactEmail" to "team@example.org\r\nX-Evil: 1")

        // Act + Assert
        assertFailsWith<IllegalArgumentException> { config.openFoodFactsSettings() }
    }

    @Test
    fun `openFoodFactsSettings refuses a value that is not one address`() {
        // Arrange
        val config = MapApplicationConfig("openFoodFacts.contactEmail" to "team at example dot org")

        // Act + Assert
        assertFailsWith<IllegalArgumentException> { config.openFoodFactsSettings() }
    }
}
