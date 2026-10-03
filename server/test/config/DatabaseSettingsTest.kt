package com.restpartijen.api.config

import io.ktor.server.config.MapApplicationConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DatabaseSettingsTest {

    @Test
    fun `mode file is read together with the file path`() {
        // Arrange
        val config = MapApplicationConfig("database.mode" to "file", "database.file" to "./data/test")
        // Act
        val settings = config.databaseSettings()
        // Assert
        assertEquals(DatabaseSettings(DbMode.FILE, "./data/test"), settings)
    }

    @Test
    fun `mode is read case insensitive`() {
        // Arrange
        val config = MapApplicationConfig("database.mode" to "FILE")
        // Act
        val settings = config.databaseSettings()
        // Assert
        assertEquals(DbMode.FILE, settings.mode)
    }

    @Test
    fun `missing section defaults to memory`() {
        // Arrange: like testApplication, which does not load application.yaml
        val config = MapApplicationConfig()
        // Act
        val settings = config.databaseSettings()
        // Assert
        assertEquals(DbMode.MEMORY, settings.mode)
    }

    @Test
    fun `blank mode defaults to memory`() {
        // Arrange
        val config = MapApplicationConfig("database.mode" to "")
        // Act
        val settings = config.databaseSettings()
        // Assert
        assertEquals(DbMode.MEMORY, settings.mode)
    }

    @Test
    fun `unknown mode stops with a message that names DB_MODE`() {
        // Arrange
        val config = MapApplicationConfig("database.mode" to "disk")
        // Act
        val exception = assertFailsWith<IllegalStateException> { config.databaseSettings() }
        // Assert
        assertTrue("DB_MODE" in exception.message.orEmpty())
    }
}