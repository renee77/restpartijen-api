package com.restpartijen.api.config

import io.ktor.server.config.ApplicationConfig


enum class DbMode { MEMORY, FILE; }

data class DatabaseSettings(val mode: DbMode, val filePath: String)


private const val DEFAULT_FILE_PATH = "./data/restpartijen"

/**
 * Reads the `database` section.
 * A missing or blank mode means MEMORY, so tests that do not load
 * application.yaml always run in memory. An unknown mode stops the application.
 */


fun ApplicationConfig.databaseSettings(): DatabaseSettings {
    val rawMode = propertyOrNull("database.mode")?.getString()?.trim().orEmpty()
    val mode = if (rawMode.isEmpty()) {
        DbMode.MEMORY
    } else {
        DbMode.entries.firstOrNull { it.name.equals(rawMode, ignoreCase = true) }
            ?: error("Invalid database.mode '$rawMode' (DB_MODE). Use 'memory' or 'file'.")
    }
    val filePath = propertyOrNull("database.file")?.getString() ?: DEFAULT_FILE_PATH
    return DatabaseSettings(mode, filePath)
}
