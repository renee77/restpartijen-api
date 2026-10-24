package com.restpartijen.api.config

import io.ktor.server.config.ApplicationConfig

/** Where H2 keeps its data, set with `database.mode` (DB_MODE). */
enum class DbMode {
    /** Empty at every start, so the seed data loads every time; the default. */
    MEMORY,

    /** Stores the data in a file, so it survives a restart. */
    FILE,
}

/**
 * The `database` section of the configuration.
 *
 * @property mode memory or file.
 * @property filePath the H2 file path without extension; only used in [DbMode.FILE].
 */
data class DatabaseSettings(val mode: DbMode, val filePath: String)

/** The file path when `database.file` is missing; the same path as in application.yaml. */
private const val DEFAULT_FILE_PATH = "./data/restpartijen"

/**
 * Reads the `database` section.
 * A missing or blank mode means MEMORY, so tests that do not load
 * application.yaml always run in memory. An unknown mode stops the application.
 * A missing `database.file` falls back to [DEFAULT_FILE_PATH].
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
